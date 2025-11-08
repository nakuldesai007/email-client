package com.emailclient.backend.email;

import com.emailclient.backend.email.dto.EmailDetail;
import com.emailclient.backend.email.dto.EmailPreview;
import com.emailclient.backend.email.dto.SendEmailRequest;

import java.util.List;
import java.util.Optional;

public interface EmailService {

    List<EmailPreview> listInbox();

    List<EmailPreview> listSent();

    List<EmailPreview> listTrash();

    void sendEmail(SendEmailRequest request);

    Optional<EmailDetail> getEmailDetail(String id);

    boolean deleteEmail(String id);

    MoveToTrashResult moveToTrash(String id);

    RestoreEmailResult restoreEmail(String id);

    boolean permanentlyDelete(String id);

    record MoveToTrashResult(boolean success, String newId) {}

    record RestoreEmailResult(boolean success, String newId) {}
}

