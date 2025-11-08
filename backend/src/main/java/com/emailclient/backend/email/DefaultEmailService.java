package com.emailclient.backend.email;

import com.emailclient.backend.email.dto.EmailDetail;
import com.emailclient.backend.email.dto.EmailPreview;
import com.emailclient.backend.email.dto.SendEmailRequest;
import com.emailclient.backend.email.smtp.SecureSmtpMailer;
import com.emailclient.backend.email.storage.EmailOfflineStore;
import com.emailclient.backend.email.storage.EmailOfflineStore.StoredEmail;
import jakarta.annotation.PostConstruct;
import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessageRemovedException;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.UIDFolder;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.AppendUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class DefaultEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(DefaultEmailService.class);
    private static final List<String> MESSAGE_FOLDERS_TO_SEARCH = List.of(
            "[Gmail]/Trash",
            "Trash",
            "Deleted Items",
            "Deleted",
            "INBOX",
            "[Gmail]/Sent Mail",
            "Sent",
            "[Gmail]/All Mail",
            "All Mail"
    );

    private final EmailClientProperties properties;
    private final EmailOfflineStore offlineStore;
    private final SecureSmtpMailer smtpMailer;
    private Session imapSession;

    public DefaultEmailService(EmailClientProperties properties, EmailOfflineStore offlineStore, SecureSmtpMailer smtpMailer) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.offlineStore = Objects.requireNonNull(offlineStore, "offlineStore");
        this.smtpMailer = Objects.requireNonNull(smtpMailer, "smtpMailer");
    }

    @PostConstruct
    void configureSession() {
        Properties sessionProperties = new Properties();
        String protocol = properties.getImap().isSsl() ? "imaps" : "imap";
        sessionProperties.put("mail.store.protocol", protocol);
        sessionProperties.put("mail." + protocol + ".host", properties.getImap().getHost());
        sessionProperties.put("mail." + protocol + ".port", String.valueOf(properties.getImap().getPort()));
        sessionProperties.put("mail." + protocol + ".ssl.enable", Boolean.toString(properties.getImap().isSsl()));
        sessionProperties.put("mail." + protocol + ".auth", "true");
        sessionProperties.put("mail.mime.address.strict", "false");
        
        // Connection pooling and optimization to reduce throttling impact
        sessionProperties.put("mail." + protocol + ".connectionpool.debug", "false");
        sessionProperties.put("mail." + protocol + ".connectionpoolsize", "1");
        sessionProperties.put("mail." + protocol + ".connectionpooltimeout", "45000"); // 45 seconds
        sessionProperties.put("mail." + protocol + ".timeout", "15000"); // 15 second read timeout
        sessionProperties.put("mail." + protocol + ".connectiontimeout", "10000"); // 10 second connect timeout
        sessionProperties.put("mail." + protocol + ".writetimeout", "10000"); // 10 second write timeout
        
        // Optimize fetching to reduce server load
        sessionProperties.put("mail." + protocol + ".fetchsize", "32768"); // 32KB fetch size
        sessionProperties.put("mail." + protocol + ".partialfetch", "false");
        
        this.imapSession = Session.getInstance(sessionProperties);
        this.imapSession.setDebug(log.isDebugEnabled());
    }

    @Override
    public List<EmailPreview> listInbox() {
        log.debug("Refreshing inbox cache before responding");
        boolean refreshed = refreshInboxCache();

        List<EmailPreview> cachedPreviews = offlineStore.loadPreviews();

        if (cachedPreviews.isEmpty() && !refreshed) {
            log.info("Cache empty after refresh attempt, performing fallback IMAP sync");
            try {
                List<StoredEmail> freshMessages = fetchAndCacheLatest();
                if (!freshMessages.isEmpty()) {
                    offlineStore.upsertMessages(freshMessages);
                }
                cachedPreviews = offlineStore.loadPreviews();
            } catch (MessagingException | IOException ex) {
                log.warn("Fallback IMAP sync failed: {}", ex.getMessage());
                return List.of();
            }
        }

        // Filter out sent emails (from current user) from inbox view
        String userEmail = properties.getImap().getUsername();
        return cachedPreviews.stream()
                .filter(preview -> !isSentByUser(preview.from(), userEmail))
                .toList();
    }
    
    private boolean refreshInboxCache() {
        try {
            List<StoredEmail> freshMessages = fetchAndCacheLatest();
            if (!freshMessages.isEmpty()) {
                offlineStore.upsertMessages(freshMessages);
            }
            return true;
        } catch (MessagingException | IOException ex) {
            log.warn("Unable to refresh inbox cache from IMAP: {}", ex.getMessage());
            return false;
        }
    }
    
    private boolean isSentByUser(String fromAddress, String userEmail) {
        if (fromAddress == null || userEmail == null) {
            return false;
        }
        // Extract email from "Name <email@domain.com>" format
        String extractedEmail = fromAddress;
        if (fromAddress.contains("<") && fromAddress.contains(">")) {
            int start = fromAddress.indexOf('<') + 1;
            int end = fromAddress.indexOf('>');
            if (start > 0 && end > start) {
                extractedEmail = fromAddress.substring(start, end);
            }
        }
        return extractedEmail.trim().equalsIgnoreCase(userEmail.trim());
    }

    @Override
    public List<EmailPreview> listTrash() {
        log.debug("Loading trash from cache");
        return offlineStore.loadTrashPreviews();
    }
    
    @Override
    public List<EmailPreview> listSent() {
        log.debug("Fetching sent emails from IMAP");
        try {
            List<StoredEmail> sentEmails = fetchSentFromFolder("[Gmail]/Sent Mail");
            return sentEmails.stream()
                    .map(StoredEmail::toPreview)
                    .toList();
        } catch (MessagingException | IOException ex) {
            log.warn("Failed to fetch sent emails: {}", ex.getMessage());
            // Try alternative folder names
            try {
                log.debug("Trying alternative sent folder name");
                List<StoredEmail> sentEmails = fetchSentFromFolder("Sent");
                return sentEmails.stream()
                        .map(StoredEmail::toPreview)
                        .toList();
            } catch (MessagingException | IOException ex2) {
                log.error("Failed to fetch sent emails from alternative folder", ex2);
                return List.of();
            }
        }
    }

    @Override
    public void sendEmail(SendEmailRequest request) {
        log.info("Attempting SMTP delivery for subject='{}'", request.subject());
        smtpMailer.send(request);
    }

    @Override
    public boolean deleteEmail(String id) {
        log.info("Attempting to delete email with id={}", id);
        Store store = null;
        Folder sourceFolder = null;
        Folder trash = null;
        try {
            String protocol = properties.getImap().isSsl() ? "imaps" : "imap";
            store = imapSession.getStore(protocol);
            
            log.debug("Connecting to IMAP server for delete operation");
            store.connect(
                    properties.getImap().getHost(),
                    properties.getImap().getPort(),
                    properties.getImap().getUsername(),
                    properties.getImap().getPassword()
            );

            // Try to find the email in multiple folders (INBOX, Sent, etc.)
            long uid = Long.parseLong(id);
            Message message = null;
            
            for (String folderName : MESSAGE_FOLDERS_TO_SEARCH) {
                try {
                    sourceFolder = store.getFolder(folderName);
                    if (sourceFolder.exists() && sourceFolder instanceof UIDFolder uidFolder) {
                        sourceFolder.open(Folder.READ_WRITE);
                        message = uidFolder.getMessageByUID(uid);
                        
                        if (message != null) {
                            log.debug("Email found in folder: {}", folderName);
                            break;
                        } else {
                            sourceFolder.close(false);
                            sourceFolder = null;
                        }
                    }
                } catch (MessagingException e) {
                    log.debug("Could not find email in folder '{}': {}", folderName, e.getMessage());
                    if (sourceFolder != null && sourceFolder.isOpen()) {
                        sourceFolder.close(false);
                    }
                    sourceFolder = null;
                }
            }

            if (message == null) {
                log.warn("Email not found with id={} in any folder", id);
                return false;
            }

            // Try Gmail trash folder first, then standard Deleted Items
            String[] trashFolderNames = {"[Gmail]/Trash", "Trash", "Deleted Items", "Deleted"};
            trash = null;
            
            for (String trashName : trashFolderNames) {
                try {
                    Folder testFolder = store.getFolder(trashName);
                    if (testFolder.exists()) {
                        trash = testFolder;
                        log.debug("Found trash folder: {}", trashName);
                        break;
                    }
                } catch (MessagingException e) {
                    log.debug("Trash folder '{}' not found, trying next", trashName);
                }
            }

            boolean alreadyInTrash = sourceFolder != null && trash != null
                    && sourceFolder.getFullName().equalsIgnoreCase(trash.getFullName());
            if (log.isDebugEnabled()) {
                log.debug("Delete email {}: source folder='{}', trash folder='{}', alreadyInTrash={}",
                        id,
                        folderName(sourceFolder),
                        folderName(trash),
                        alreadyInTrash);
            }

            if (trash != null && trash.exists() && !alreadyInTrash) {
                // Move to trash
                trash.open(Folder.READ_WRITE);
                if (sourceFolder != null) {
                sourceFolder.copyMessages(new Message[]{message}, trash);
                } else {
                    log.warn("Source folder lost before copying message {} to trash", id);
                }
                trash.close(false);
                log.info("Email moved to trash folder");
            } else if (alreadyInTrash) {
                log.debug("Email {} already in trash folder {}, skipping copy step", id, trash != null ? trash.getFullName() : "unknown");
            }

            // Mark for deletion and expunge
            try {
            message.setFlag(Flags.Flag.DELETED, true);
                if (sourceFolder != null) {
            sourceFolder.expunge();
                }
            } catch (MessageRemovedException removedEx) {
                log.debug("Message {} was already removed from folder {} during delete", id, folderName(sourceFolder));
            }

            boolean removedLocally = offlineStore.permanentlyDelete(id);
            if (!removedLocally) {
                log.warn("Email {} could not be removed from offline cache after delete", id);
            }
            
            log.info("Email successfully deleted: id={}", id);
            return true;

        } catch (Exception ex) {
            log.error("Failed to delete email with id={}", id, ex);
            return false;
        } finally {
            try {
                if (trash != null && trash.isOpen()) {
                    trash.close(false);
                }
                if (sourceFolder != null && sourceFolder.isOpen()) {
                    sourceFolder.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException ex) {
                log.warn("Error closing IMAP connection", ex);
            }
        }
    }
    
    @Override
    public MoveToTrashResult moveToTrash(String id) {
        log.info("Moving email to trash: id={}", id);
        boolean localSuccess = offlineStore.markAsTrashed(id);
        String effectiveId = id;

        if (!localSuccess) {
            log.info("Email {} missing from offline cache during trash operation, attempting to hydrate before proceeding", id);
            EmailOfflineStore.StoredEmail cachedEmail = offlineStore.loadEmailById(id).orElse(null);
            if (cachedEmail == null) {
                EmailOfflineStore.StoredEmail fetchedEmail = fetchEmailFromImap(id);
                if (fetchedEmail != null) {
                    offlineStore.upsertMessages(List.of(fetchedEmail));
                    cachedEmail = fetchedEmail;
                } else {
                    log.warn("Unable to fetch email {} from IMAP while preparing to trash", id);
                }
            }
            if (cachedEmail != null) {
                String cacheId = cachedEmail.id();
                if (!Objects.equals(cacheId, id)) {
                    log.debug("Hydrated email {} using cache id {} prior to trash operation", id, cacheId);
                }
                localSuccess = offlineStore.markAsTrashed(cacheId);
                if (localSuccess) {
                    effectiveId = cacheId;
                }
            }
        }

        if (!localSuccess) {
            log.warn("Failed to mark email as trashed locally after hydration attempt: id={}", id);
            return new MoveToTrashResult(false, effectiveId);
        }
        
        Store store = null;
        Folder sourceFolder = null;
        Folder trash = null;
        try {
            String protocol = properties.getImap().isSsl() ? "imaps" : "imap";
            store = imapSession.getStore(protocol);
            
            log.debug("Connecting to IMAP server for trash operation");
            store.connect(
                    properties.getImap().getHost(),
                    properties.getImap().getPort(),
                    properties.getImap().getUsername(),
                    properties.getImap().getPassword()
            );

            long uid = Long.parseLong(effectiveId);
            Message message = null;
            String messageIdHeader = null;
            
            for (String folderName : MESSAGE_FOLDERS_TO_SEARCH) {
                try {
                    sourceFolder = store.getFolder(folderName);
                    if (sourceFolder.exists() && sourceFolder instanceof UIDFolder uidFolder) {
                        sourceFolder.open(Folder.READ_WRITE);
                        message = uidFolder.getMessageByUID(uid);
                        
                        if (message != null) {
                            log.debug("Email found in folder: {}", folderName);
                            messageIdHeader = getMessageId(message);
                            log.debug("Message {} message-id header: {}", effectiveId, messageIdHeader);
                            break;
                        } else {
                            sourceFolder.close(false);
                            sourceFolder = null;
                        }
                    }
                } catch (MessagingException e) {
                    log.debug("Could not find email in folder '{}': {}", folderName, e.getMessage());
                    if (sourceFolder != null && sourceFolder.isOpen()) {
                        sourceFolder.close(false);
                    }
                    sourceFolder = null;
                }
            }

            if (message == null) {
                log.warn("Email not found on server with id={}, but marked as trashed locally", effectiveId);
                return new MoveToTrashResult(true, effectiveId);
            }

            String[] trashFolderNames = {"[Gmail]/Trash", "Trash", "Deleted Items", "Deleted"};
            for (String trashName : trashFolderNames) {
                try {
                    Folder testFolder = store.getFolder(trashName);
                    if (testFolder.exists()) {
                        trash = testFolder;
                        log.debug("Found trash folder: {}", trashName);
                        break;
                    }
                } catch (MessagingException e) {
                    log.debug("Trash folder '{}' not found, trying next", trashName);
                }
            }

            if (trash != null && trash.exists()) {
                trash.open(Folder.READ_WRITE);
                UIDFolder trashUidFolder = trash instanceof UIDFolder t ? t : null;
                IMAPFolder imapTrashFolder = trash instanceof IMAPFolder t ? t : null;
                IMAPFolder imapSourceFolder = sourceFolder instanceof IMAPFolder s ? s : null;
                long expectedUid = -1;
                if (trashUidFolder != null) {
                    try {
                        expectedUid = trashUidFolder.getUIDNext();
                    } catch (MessagingException ex) {
                        log.debug("Unable to read UIDNEXT from trash folder: {}", ex.getMessage());
                    }
                }

                Long newUidFromServer = null;
                boolean copied = false;

                if (sourceFolder != null) {
                    if (imapSourceFolder != null && imapTrashFolder != null) {
                        try {
                            AppendUID[] appendUids = imapSourceFolder.copyUIDMessages(new Message[]{message}, imapTrashFolder);
                            copied = true;
                            if (appendUids != null && appendUids.length > 0) {
                                newUidFromServer = appendUids[0].uid;
                            }
                        } catch (MessagingException ex) {
                            log.debug("Unable to copy message {} with UID mapping support: {}", id, ex.getMessage());
                        }
                    }
                    if (!copied) {
                        sourceFolder.copyMessages(new Message[]{message}, trash);
                        copied = true;
                    }
                } else {
                    log.warn("Source folder lost before copying message {} to trash", id);
                }

                if (newUidFromServer != null && newUidFromServer > 0) {
                    String newId = Long.toString(newUidFromServer);
                    if (!Objects.equals(newId, id)) {
                        if (offlineStore.updateMessageId(id, newId)) {
                            log.debug("Updated cached UID for message {} to {}", id, newId);
                            effectiveId = newId;
                        } else {
                            log.warn("Unable to update cached UID for message {} to {}", id, newId);
                        }
                    }
                }

                if (trashUidFolder != null && messageIdHeader != null && expectedUid > 0 && newUidFromServer == null) {
                    try {
                        long startUid = Math.max(1, expectedUid - 5);
                        Message[] candidates = trashUidFolder.getMessagesByUID(startUid, Long.MAX_VALUE);
                        if (candidates.length > 0) {
                            FetchProfile profile = new FetchProfile();
                            profile.add(FetchProfile.Item.ENVELOPE);
                            profile.add(UIDFolder.FetchProfileItem.UID);
                            trash.fetch(candidates, profile);
                            for (Message candidate : candidates) {
                                String candidateMessageId = getMessageId(candidate);
                                if (candidateMessageId != null && candidateMessageId.equalsIgnoreCase(messageIdHeader)) {
                                    long newUid = trashUidFolder.getUID(candidate);
                                    if (newUid > 0) {
                                        String newId = Long.toString(newUid);
                                        if (!newId.equals(id)) {
                                            if (offlineStore.updateMessageId(id, newId)) {
                                                log.debug("Updated cached UID for message {} to {}", id, newId);
                                                effectiveId = newId;
                                            } else {
                                                log.warn("Unable to update cached UID for message {} to {}", id, newId);
                                            }
                                        } else {
                                            log.debug("UID for message {} unchanged after copy", id);
                                        }
                                    }
                                    break;
                                }
                            }
                        } else {
                            log.debug("No new candidates found in trash for message {}", id);
                        }
                    } catch (MessagingException ex) {
                        log.debug("Unable to resolve new UID after copying message {} to trash: {}", id, ex.getMessage());
                    }
                } else if (messageIdHeader == null) {
                    log.debug("Message {} missing Message-ID header; unable to reconcile UID after copy", id);
                }

                trash.close(false);
                
                try {
                message.setFlag(Flags.Flag.DELETED, true);
                    if (sourceFolder != null) {
                sourceFolder.expunge();
                    }
                } catch (MessageRemovedException removedEx) {
                    log.debug("Message {} already removed from folder {} while moving to trash", id, folderName(sourceFolder));
                }
                
                log.info("Email moved to trash on server: id={}", id);
            }

            return new MoveToTrashResult(true, effectiveId);

        } catch (Exception ex) {
            log.error("Failed to move email to trash on server: id={}", id, ex);
            if (localSuccess) {
                boolean reverted = offlineStore.unmarkAsTrashed(effectiveId);
                if (!reverted && !Objects.equals(effectiveId, id)) {
                    offlineStore.unmarkAsTrashed(id);
                }
            }
            return new MoveToTrashResult(false, effectiveId);
        } finally {
            try {
                if (trash != null && trash.isOpen()) {
                    trash.close(false);
                }
                if (sourceFolder != null && sourceFolder.isOpen()) {
                    sourceFolder.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException ex) {
                log.warn("Error closing IMAP connection", ex);
            }
        }
    }
    
    @Override
    public RestoreEmailResult restoreEmail(String id) {
        log.info("Restoring email from trash: id={}", id);
        boolean localSuccess = offlineStore.unmarkAsTrashed(id);
        String effectiveId = id;

        if (!localSuccess) {
            log.info("Email {} missing from offline cache during restore, attempting to hydrate", id);
            EmailOfflineStore.StoredEmail cachedEmail = offlineStore.loadEmailById(id).orElse(null);
            if (cachedEmail == null) {
                EmailOfflineStore.StoredEmail fetchedEmail = fetchEmailFromImap(id);
                if (fetchedEmail != null) {
                    offlineStore.upsertMessages(List.of(fetchedEmail));
                    cachedEmail = fetchedEmail;
                } else {
                    log.warn("Unable to fetch email {} from IMAP while preparing restore", id);
                }
            }
            if (cachedEmail != null) {
                String cacheId = cachedEmail.id();
                if (!Objects.equals(cacheId, id)) {
                    log.debug("Hydrated email {} using cache id {} prior to restore", id, cacheId);
                }
                localSuccess = offlineStore.unmarkAsTrashed(cacheId);
                if (localSuccess) {
                    effectiveId = cacheId;
                }
            }
        }

        if (!localSuccess) {
            log.warn("Failed to update local cache to restore email: id={}", id);
            return new RestoreEmailResult(false, effectiveId);
        }

        Store store = null;
        Folder trash = null;
        Folder inbox = null;
        try {
            String protocol = properties.getImap().isSsl() ? "imaps" : "imap";
            store = imapSession.getStore(protocol);

            log.debug("Connecting to IMAP server for restore operation");
            store.connect(
                    properties.getImap().getHost(),
                    properties.getImap().getPort(),
                    properties.getImap().getUsername(),
                    properties.getImap().getPassword()
            );

            long uid = Long.parseLong(effectiveId);
            Message message = null;
            String messageIdHeader = null;

            String[] trashFolderNames = {"[Gmail]/Trash", "Trash", "Deleted Items", "Deleted"};
            for (String trashName : trashFolderNames) {
                try {
                    trash = store.getFolder(trashName);
                    if (trash.exists() && trash instanceof UIDFolder uidFolder) {
                        trash.open(Folder.READ_WRITE);
                        message = uidFolder.getMessageByUID(uid);
                        if (message != null) {
                            log.debug("Email found in trash folder: {}", trashName);
                            messageIdHeader = getMessageId(message);
                            break;
                        } else {
                            trash.close(false);
                            trash = null;
                        }
                    }
                } catch (MessagingException ex) {
                    log.debug("Unable to find message {} in trash folder '{}': {}", id, trashName, ex.getMessage());
                    if (trash != null && trash.isOpen()) {
                        trash.close(false);
                    }
                    trash = null;
                }
            }

            if (message == null) {
                log.warn("Email not found in trash on server with id={}, but restored locally", effectiveId);
                return new RestoreEmailResult(true, effectiveId);
            }

            inbox = store.getFolder("INBOX");
            if (inbox == null || !inbox.exists()) {
                log.warn("INBOX folder not available during restore operation");
                return new RestoreEmailResult(true, effectiveId);
            }

            inbox.open(Folder.READ_WRITE);
            UIDFolder inboxUidFolder = inbox instanceof UIDFolder u ? u : null;
            IMAPFolder imapInbox = inbox instanceof IMAPFolder i ? i : null;
            IMAPFolder imapTrash = (trash instanceof IMAPFolder) ? (IMAPFolder) trash : null;
            long expectedUid = -1;
            if (inboxUidFolder != null) {
                try {
                    expectedUid = inboxUidFolder.getUIDNext();
                } catch (MessagingException ex) {
                    log.debug("Unable to read UIDNEXT from inbox folder: {}", ex.getMessage());
                }
            }

            Long newUidFromServer = null;
            boolean copied = false;
            if (imapTrash != null && imapInbox != null) {
                try {
                    AppendUID[] appendUids = imapTrash.copyUIDMessages(new Message[]{message}, imapInbox);
                    copied = true;
                    if (appendUids != null && appendUids.length > 0) {
                        newUidFromServer = appendUids[0].uid;
                    }
                } catch (MessagingException ex) {
                    log.debug("Unable to copy message {} with UID mapping support to inbox: {}", id, ex.getMessage());
                }
            }
            if (!copied && trash != null) {
                trash.copyMessages(new Message[]{message}, inbox);
                copied = true;
            }

            if (!copied) {
                log.warn("Unable to copy message {} from trash to inbox", id);
                return new RestoreEmailResult(true, effectiveId);
            }

            if (newUidFromServer != null && newUidFromServer > 0) {
                String newId = Long.toString(newUidFromServer);
                if (!Objects.equals(newId, effectiveId)) {
                    if (offlineStore.updateMessageId(effectiveId, newId)) {
                        log.debug("Updated cached UID for restored message {} to {}", effectiveId, newId);
                        effectiveId = newId;
                    } else {
                        log.warn("Unable to update cached UID for restored message {} to {}", effectiveId, newId);
                    }
                }
            } else if (messageIdHeader != null && inboxUidFolder != null && expectedUid > 0) {
                try {
                    long startUid = Math.max(1, expectedUid - 10);
                    Message[] candidates = inboxUidFolder.getMessagesByUID(startUid, Long.MAX_VALUE);
                    if (candidates.length > 0) {
                        FetchProfile profile = new FetchProfile();
                        profile.add(FetchProfile.Item.ENVELOPE);
                        profile.add(UIDFolder.FetchProfileItem.UID);
                        inbox.fetch(candidates, profile);
                        for (Message candidate : candidates) {
                            String candidateMessageId = getMessageId(candidate);
                            if (candidateMessageId != null && candidateMessageId.equalsIgnoreCase(messageIdHeader)) {
                                long newUid = inboxUidFolder.getUID(candidate);
                                if (newUid > 0) {
                                    String newId = Long.toString(newUid);
                                    if (!Objects.equals(newId, effectiveId)) {
                                        if (offlineStore.updateMessageId(effectiveId, newId)) {
                                            log.debug("Updated cached UID for restored message {} to {}", effectiveId, newId);
                                            effectiveId = newId;
                                        } else {
                                            log.warn("Unable to update cached UID for restored message {} to {}", effectiveId, newId);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                } catch (MessagingException ex) {
                    log.debug("Unable to reconcile inbox UID after restore for message {}: {}", id, ex.getMessage());
                }
            }

            try {
                message.setFlag(Flags.Flag.DELETED, true);
                if (trash != null) {
                    trash.expunge();
                }
            } catch (MessageRemovedException removedEx) {
                log.debug("Message {} already removed from trash while restoring", id);
            }

            log.info("Email restored from trash: id={}", effectiveId);
            return new RestoreEmailResult(true, effectiveId);
        } catch (Exception ex) {
            log.error("Failed to restore email from trash: id={}", id, ex);
            offlineStore.markAsTrashed(effectiveId);
            return new RestoreEmailResult(false, effectiveId);
        } finally {
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false);
                }
                if (trash != null && trash.isOpen()) {
                    trash.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException ex) {
                log.warn("Error closing IMAP connection during restore", ex);
            }
        }
    }
    
    @Override
    public boolean permanentlyDelete(String id) {
        log.info("Permanently deleting email: id={}", id);
        
        // Check if email is actually in trash
        if (!offlineStore.isTrashed(id)) {
            log.warn("Email {} is not in trash, cannot permanently delete", id);
            return false;
        }
        
        // Delete from local database
        boolean localSuccess = offlineStore.permanentlyDelete(id);
        
        if (!localSuccess) {
            log.warn("Failed to permanently delete email from database: id={}", id);
            return false;
        }
        
        // Try to delete from trash folder on server
        Store store = null;
        Folder trashFolder = null;
        try {
            String protocol = properties.getImap().isSsl() ? "imaps" : "imap";
            store = imapSession.getStore(protocol);
            
            log.debug("Connecting to IMAP server for permanent delete");
            store.connect(
                    properties.getImap().getHost(),
                    properties.getImap().getPort(),
                    properties.getImap().getUsername(),
                    properties.getImap().getPassword()
            );

            long uid = Long.parseLong(id);
            
            // Try Gmail trash folder first, then standard trash folders
            String[] trashFolderNames = {"[Gmail]/Trash", "Trash", "Deleted Items", "Deleted"};
            
            for (String folderName : trashFolderNames) {
                try {
                    trashFolder = store.getFolder(folderName);
                    if (trashFolder.exists() && trashFolder instanceof UIDFolder uidFolder) {
                        trashFolder.open(Folder.READ_WRITE);
                        Message message = uidFolder.getMessageByUID(uid);
                        
                        if (message != null) {
                            log.debug("Email found in trash folder: {}", folderName);
                            message.setFlag(Flags.Flag.DELETED, true);
                            trashFolder.expunge();
                            log.info("Email permanently deleted from server trash: id={}", id);
                            break;
                        } else {
                            trashFolder.close(false);
                            trashFolder = null;
                        }
                    }
                } catch (MessagingException e) {
                    log.debug("Could not find email in trash folder '{}': {}", folderName, e.getMessage());
                    if (trashFolder != null && trashFolder.isOpen()) {
                        trashFolder.close(false);
                    }
                    trashFolder = null;
                }
            }

            if (trashFolder == null) {
                log.warn("Email not found in server trash with id={}, but deleted locally", id);
            }

            return true;

        } catch (Exception ex) {
            log.error("Failed to permanently delete email from server: id={}", id, ex);
            // Still return true since local delete succeeded
            return true;
        } finally {
            try {
                if (trashFolder != null && trashFolder.isOpen()) {
                    trashFolder.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException ex) {
                log.warn("Error closing IMAP connection", ex);
            }
        }
    }

    @Override
    public java.util.Optional<EmailDetail> getEmailDetail(String id) {
        log.debug("Fetching email detail for id={}", id);
        
        // Try to load from cache first
        java.util.Optional<StoredEmail> cachedEmail = offlineStore.loadEmailById(id);
        
        if (cachedEmail.isPresent()) {
            StoredEmail storedEmail = cachedEmail.get();
            if (storedEmail.unread() && offlineStore.markAsRead(id)) {
                storedEmail = new StoredEmail(
                        storedEmail.id(),
                        storedEmail.from(),
                        storedEmail.subject(),
                        storedEmail.receivedAt(),
                        false,
                        storedEmail.rawMessage());
            }
            try {
                if (storedEmail.rawMessage() == null || storedEmail.rawMessage().length == 0) {
                    log.info("Raw message not cached for id={}, fetching from IMAP", id);
                    StoredEmail fetchedEmail = fetchEmailFromImap(id);
                    if (fetchedEmail != null) {
                        offlineStore.upsertMessages(List.of(fetchedEmail));
                        if (fetchedEmail.unread() && offlineStore.markAsRead(id)) {
                            fetchedEmail = new StoredEmail(
                                    fetchedEmail.id(),
                                    fetchedEmail.from(),
                                    fetchedEmail.subject(),
                                    fetchedEmail.receivedAt(),
                                    false,
                                    fetchedEmail.rawMessage());
                        }
                        return java.util.Optional.ofNullable(parseEmailDetail(fetchedEmail));
                    }
                }
                return java.util.Optional.ofNullable(parseEmailDetail(storedEmail));
            } catch (Exception ex) {
                log.error("Failed to parse email detail for id={}", id, ex);
                return java.util.Optional.empty();
            }
        }
        
        // Email not in cache (e.g., sent emails), fetch directly from IMAP
        log.info("Email id={} not in cache, fetching from IMAP", id);
        try {
            StoredEmail fetchedEmail = fetchEmailFromImap(id);
            if (fetchedEmail != null) {
                // Cache it for future use
                offlineStore.upsertMessages(List.of(fetchedEmail));
                return java.util.Optional.ofNullable(parseEmailDetail(fetchedEmail));
            }
        } catch (Exception ex) {
            log.error("Failed to fetch email from IMAP for id={}", id, ex);
        }
        
        return java.util.Optional.empty();
    }

    private StoredEmail fetchEmailFromImap(String messageId) {
        Store store = null;
        Folder folder = null;
        try {
            String protocol = properties.getImap().isSsl() ? "imaps" : "imap";
            store = imapSession.getStore(protocol);
            
            log.debug("Connecting to IMAP server for message id={}", messageId);
            store.connect(
                    properties.getImap().getHost(),
                    properties.getImap().getPort(),
                    properties.getImap().getUsername(),
                    properties.getImap().getPassword()
            );

            // Try to find the email in multiple folders (INBOX, Sent, etc.)
            long uid = Long.parseLong(messageId);
            Message message = null;
            UIDFolder uidFolder = null;
            
            for (String folderName : MESSAGE_FOLDERS_TO_SEARCH) {
                try {
                    folder = store.getFolder(folderName);
                    if (folder.exists() && folder instanceof UIDFolder) {
                        folder.open(Folder.READ_WRITE);
                        uidFolder = (UIDFolder) folder;
                        message = uidFolder.getMessageByUID(uid);
                        
                        if (message != null) {
                            log.debug("Email found in folder: {}", folderName);
                            break;
                        } else {
                            folder.close(false);
                            folder = null;
                        }
                    }
                } catch (MessagingException e) {
                    log.debug("Could not find email in folder '{}': {}", folderName, e.getMessage());
                    if (folder != null && folder.isOpen()) {
                        folder.close(false);
                    }
                    folder = null;
                }
            }

            if (message != null && uidFolder != null) {
                message.setFlag(Flags.Flag.SEEN, true);
                // Optimized fetch profile - fetch everything in one go
                FetchProfile profile = new FetchProfile();
                profile.add(FetchProfile.Item.ENVELOPE);
                profile.add(FetchProfile.Item.FLAGS);
                profile.add(FetchProfile.Item.CONTENT_INFO);
                profile.add(UIDFolder.FetchProfileItem.UID);
                profile.add("X-mailer"); // Reduce extra round-trips
                if (folder != null) {
                folder.fetch(new Message[]{message}, profile);
                }

                StoredEmail result = toStoredEmailWithContent(uidFolder, message);
                
                log.info("Successfully fetched email content for id={}", messageId);
                return result;
            }

            log.warn("Email not found in IMAP for id={}", messageId);
        } catch (Exception ex) {
            log.error("Failed to fetch email from IMAP for id={}", messageId, ex);
        } finally {
            // Ensure resources are properly closed
            try {
                if (folder != null && folder.isOpen()) {
                    folder.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException ex) {
                log.warn("Error closing IMAP connection", ex);
            }
        }
        return null;
    }

    private String getMessageId(Message message) throws MessagingException {
        if (message == null) {
            return null;
        }
        if (message instanceof MimeMessage mimeMessage) {
            String messageId = mimeMessage.getMessageID();
            if (messageId != null && !messageId.isBlank()) {
                return messageId;
            }
        }
        String[] headers = message.getHeader("Message-ID");
        if (headers != null) {
            for (String header : headers) {
                if (header != null && !header.isBlank()) {
                    return header;
                }
            }
        }
        return null;
    }

    private String folderName(Folder folder) {
        return folder != null ? folder.getFullName() : "unknown";
    }

    private StoredEmail toStoredEmailWithContent(UIDFolder folder, Message message) throws MessagingException, IOException {
        long uid = folder.getUID(message);
        String messageId = Long.toString(uid);
        String fromAddress = Arrays.stream(message.getFrom() != null ? message.getFrom() : new InternetAddress[0])
                .findFirst()
                .map(address -> {
                    if (address instanceof InternetAddress internetAddress) {
                        return internetAddress.toUnicodeString();
                    }
                    return address.toString();
                })
                .orElse("unknown");

        OffsetDateTime receivedAt = null;
        if (message.getReceivedDate() != null) {
            receivedAt = message.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        } else if (message.getSentDate() != null) {
            receivedAt = message.getSentDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }

        boolean unread = !message.isSet(Flags.Flag.SEEN);
        byte[] raw = extractRawBytes(message);

        return new StoredEmail(messageId, fromAddress, message.getSubject(), receivedAt, unread, raw);
    }

    private EmailDetail parseEmailDetail(StoredEmail storedEmail) throws MessagingException, IOException {
        // If raw message is available, parse it for full details
        if (storedEmail.rawMessage() != null && storedEmail.rawMessage().length > 0) {
            Message message = new jakarta.mail.internet.MimeMessage(
                    imapSession,
                    storedEmail.rawMessageStream()
            );
            
            String body = extractTextBody(message);
            List<String> to = extractAddresses(message.getRecipients(Message.RecipientType.TO));
            List<String> cc = extractAddresses(message.getRecipients(Message.RecipientType.CC));
            
            return new EmailDetail(
                    storedEmail.id(),
                    storedEmail.from(),
                    storedEmail.subject(),
                    body,
                    storedEmail.receivedAt(),
                    storedEmail.unread(),
                    to,
                    cc
            );
        } else {
            // Fallback: return basic info without body if raw message not available
            return new EmailDetail(
                    storedEmail.id(),
                    storedEmail.from(),
                    storedEmail.subject(),
                    "Email content not available (unable to fetch from server)",
                    storedEmail.receivedAt(),
                    storedEmail.unread(),
                    List.of(),
                    List.of()
            );
        }
    }

    private String extractTextBody(jakarta.mail.Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain")) {
            return part.getContent().toString();
        } else if (part.isMimeType("text/html")) {
            // Return HTML as-is for now, could be rendered in frontend
            return part.getContent().toString();
        } else if (part.isMimeType("multipart/*")) {
            jakarta.mail.Multipart multipart = (jakarta.mail.Multipart) part.getContent();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                jakarta.mail.BodyPart bodyPart = multipart.getBodyPart(i);
                String partText = extractTextBody(bodyPart);
                if (partText != null && !partText.isEmpty()) {
                    result.append(partText);
                    // Prefer plain text, return early if found
                    if (bodyPart.isMimeType("text/plain")) {
                        return partText;
                    }
                }
            }
            return result.toString();
        }
        return "";
    }

    private List<String> extractAddresses(jakarta.mail.Address[] addresses) {
        if (addresses == null) {
            return List.of();
        }
        return Arrays.stream(addresses)
                .map(address -> {
                    if (address instanceof InternetAddress ia) {
                        return ia.toUnicodeString();
                    }
                    return address.toString();
                })
                .toList();
    }

    private List<StoredEmail> fetchAndCacheLatest() throws MessagingException, IOException {
        return fetchFromFolder("INBOX");
    }

    private List<StoredEmail> fetchFromFolder(String folderName) throws MessagingException, IOException {
        String protocol = properties.getImap().isSsl() ? "imaps" : "imap";
        Store store = imapSession.getStore(protocol);
        try {
            log.debug("Connecting to IMAP for bulk sync from folder: {}", folderName);
            store.connect(
                    properties.getImap().getHost(),
                    properties.getImap().getPort(),
                    properties.getImap().getUsername(),
                    properties.getImap().getPassword()
            );

            Folder folder = store.getFolder(folderName);
            if (!(folder instanceof UIDFolder uidFolder)) {
                throw new MessagingException("IMAP store does not provide UID support");
            }

            folder.open(Folder.READ_ONLY);

            int messageCount = folder.getMessageCount();
            if (messageCount == 0) {
                folder.close(false);
                log.info("No messages in folder: {}", folderName);
                return List.of();
            }

            int batchSize = Math.max(1, properties.getImap().getFetchBatchSize());
            int start = Math.max(1, messageCount - batchSize + 1);
            Message[] messages = folder.getMessages(start, messageCount);

            // Optimized fetch profile - get everything we need in one round-trip
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            profile.add(FetchProfile.Item.FLAGS);
            profile.add(UIDFolder.FetchProfileItem.UID);
            profile.add("X-mailer"); // Prevent lazy loading
            
            log.debug("Fetching {} messages from folder: {}", messages.length, folderName);
            folder.fetch(messages, profile);

            List<StoredEmail> storedEmails = new ArrayList<>(messages.length);
            for (Message message : messages) {
                StoredEmail storedEmail = toStoredEmail(uidFolder, message);
                storedEmails.add(storedEmail);
            }

            // For inbox, also fetch unseen messages beyond batch
            if ("INBOX".equals(folderName)) {
                Message[] unseenMessages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                if (unseenMessages.length > 0) {
                    folder.fetch(unseenMessages, profile);
                    for (Message message : unseenMessages) {
                        StoredEmail storedEmail = toStoredEmail(uidFolder, message);
                        storedEmails.add(storedEmail);
                    }
                }
            }

            folder.close(false);

            return storedEmails.stream()
                    .collect(Collectors.collectingAndThen(Collectors.toMap(StoredEmail::id, storedEmail -> storedEmail, (first, second) -> second),
                            map -> map.values().stream()
                                    .sorted(Comparator.comparing(StoredEmail::receivedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                                    .toList()));
        } finally {
            if (store.isConnected()) {
                store.close();
            }
        }
    }

    /**
     * Fetch emails from a sent folder - uses TO address instead of FROM address for previews
     */
    private List<StoredEmail> fetchSentFromFolder(String folderName) throws MessagingException, IOException {
        String protocol = properties.getImap().isSsl() ? "imaps" : "imap";
        Store store = imapSession.getStore(protocol);
        if (!store.isConnected()) {
            store.connect(
                    properties.getImap().getHost(),
                    properties.getImap().getPort(),
                    properties.getImap().getUsername(),
                    properties.getImap().getPassword()
            );
        }

        try {
            Folder folder = store.getFolder(folderName);
            folder.open(Folder.READ_ONLY);

            if (!(folder instanceof UIDFolder uidFolder)) {
                throw new MessagingException("Folder does not support UID operations");
            }

            int messageCount = folder.getMessageCount();
            if (messageCount == 0) {
                folder.close(false);
                return List.of();
            }

            // Fetch only the latest 50 messages (or fewer if less than 50 exist)
            int limit = 50;
            int startIndex = Math.max(1, messageCount - limit + 1);
            int endIndex = messageCount;
            
            log.debug("Fetching {} latest sent messages (from {} to {}) out of {} total", 
                    Math.min(limit, messageCount), startIndex, endIndex, messageCount);

            Message[] messages = folder.getMessages(startIndex, endIndex);
            List<StoredEmail> storedEmails = new ArrayList<>();

            for (Message message : messages) {
                try {
                    StoredEmail email = toStoredSentEmail(uidFolder, message);
                    storedEmails.add(email);
                } catch (Exception ex) {
                    log.warn("Failed to parse message in sent folder: {}", ex.getMessage());
                }
            }

            folder.close(false);

            return storedEmails.stream()
                    .collect(Collectors.collectingAndThen(Collectors.toMap(StoredEmail::id, storedEmail -> storedEmail, (first, second) -> second),
                            map -> map.values().stream()
                                    .sorted(Comparator.comparing(StoredEmail::receivedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                                    .toList()));
        } finally {
            if (store.isConnected()) {
                store.close();
            }
        }
    }

    private StoredEmail toStoredEmail(UIDFolder folder, Message message) throws MessagingException, IOException {
        long uid = folder.getUID(message);
        String messageId = Long.toString(uid);
        String fromAddress = Arrays.stream(message.getFrom() != null ? message.getFrom() : new InternetAddress[0])
                .findFirst()
                .map(address -> {
                    if (address instanceof InternetAddress internetAddress) {
                        return internetAddress.toUnicodeString();
                    }
                    return address.toString();
                })
                .orElse("unknown");

        OffsetDateTime receivedAt = null;
        if (message.getReceivedDate() != null) {
            receivedAt = message.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        } else if (message.getSentDate() != null) {
            receivedAt = message.getSentDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }

        boolean unread = !message.isSet(Flags.Flag.SEEN);
        
        // Skip downloading raw bytes for faster sync - we only need them when opening individual emails
        byte[] raw = null;

        return new StoredEmail(messageId, fromAddress, message.getSubject(), receivedAt, unread, raw);
    }

    private StoredEmail toStoredSentEmail(UIDFolder folder, Message message) throws MessagingException, IOException {
        long uid = folder.getUID(message);
        String messageId = Long.toString(uid);
        
        // For sent emails, use the TO address instead of FROM address
        String toAddress = Arrays.stream(message.getRecipients(Message.RecipientType.TO) != null 
                ? message.getRecipients(Message.RecipientType.TO) : new InternetAddress[0])
                .findFirst()
                .map(address -> {
                    if (address instanceof InternetAddress internetAddress) {
                        return internetAddress.toUnicodeString();
                    }
                    return address.toString();
                })
                .orElse("unknown");

        OffsetDateTime receivedAt = null;
        if (message.getSentDate() != null) {
            receivedAt = message.getSentDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        } else if (message.getReceivedDate() != null) {
            receivedAt = message.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }

        boolean unread = !message.isSet(Flags.Flag.SEEN);
        
        // Skip downloading raw bytes for faster sync - we only need them when opening individual emails
        byte[] raw = null;

        return new StoredEmail(messageId, toAddress, message.getSubject(), receivedAt, unread, raw);
    }

    private byte[] extractRawBytes(Message message) throws IOException, MessagingException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            message.writeTo(outputStream);
            return outputStream.toByteArray();
        }
    }
}

