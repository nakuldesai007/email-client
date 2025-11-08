package com.emailclient.backend.email.storage;

import com.emailclient.backend.email.dto.EmailPreview;
import com.emailclient.backend.email.EmailClientProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Locale;

@Component
public class EmailOfflineStore {

    private static final Logger log = LoggerFactory.getLogger(EmailOfflineStore.class);

    private final DataSource dataSource;
    private final int previewLimit;

    public EmailOfflineStore(DataSource dataSource, EmailClientProperties properties) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(properties, "properties");
        this.previewLimit = properties.getStorage().getPreviewLimit();
    }

    @PostConstruct
    public void initialize() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS email_messages (
                        id VARCHAR(255) PRIMARY KEY,
                        sender TEXT NOT NULL,
                        subject TEXT,
                        received_at TIMESTAMP WITH TIME ZONE,
                        unread BOOLEAN NOT NULL,
                        trashed BOOLEAN NOT NULL DEFAULT FALSE,
                        raw BYTEA
                    )
                    """);
            
            ensureColumnExists(connection, "email_messages", "trashed",
                    "ALTER TABLE email_messages ADD COLUMN trashed BOOLEAN NOT NULL DEFAULT FALSE");

            boolean isSqlite = isSqlite(connection);
            String receivedAtIndexSql = isSqlite
                    ? "CREATE INDEX IF NOT EXISTS idx_received_at ON email_messages(received_at DESC)"
                    : "CREATE INDEX IF NOT EXISTS idx_received_at ON email_messages(received_at DESC NULLS LAST)";
            
            // Create index for faster queries
            statement.execute(receivedAtIndexSql);
            
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_trashed 
                    ON email_messages(trashed)
                    """);
            
            log.info("Email messages table initialized");
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize PostgreSQL database", ex);
        }
    }

    private boolean isSqlite(Connection connection) throws SQLException {
        return "SQLite".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
    }

    private void ensureColumnExists(Connection connection, String tableName, String columnName, String alterStatement) throws SQLException {
        if (columnExists(connection, tableName, columnName)) {
            return;
        }

        try (Statement alter = connection.createStatement()) {
            alter.execute(alterStatement);
            log.info("Added missing column '{}' to table '{}'", columnName, tableName);
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String normalizedTable = tableName;
        String normalizedColumn = columnName;

        if ("SQLite".equalsIgnoreCase(metaData.getDatabaseProductName())) {
            normalizedTable = tableName.toLowerCase(Locale.ROOT);
            normalizedColumn = columnName.toLowerCase(Locale.ROOT);
        }

        try (ResultSet columns = metaData.getColumns(null, null, normalizedTable, normalizedColumn)) {
            if (columns.next()) {
                return true;
            }
        }

        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            while (columns.next()) {
                String existingColumn = columns.getString("COLUMN_NAME");
                if (existingColumn != null && existingColumn.equalsIgnoreCase(columnName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void upsertMessages(List<StoredEmail> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                             INSERT INTO email_messages (id, sender, subject, received_at, unread, trashed, raw)
                             VALUES (?, ?, ?, ?, ?, FALSE, ?)
                             ON CONFLICT(id) DO UPDATE SET
                                 sender = EXCLUDED.sender,
                                 subject = EXCLUDED.subject,
                                 received_at = EXCLUDED.received_at,
                                 unread = EXCLUDED.unread,
                                 raw = EXCLUDED.raw
                             """)) {

            connection.setAutoCommit(false);

            for (StoredEmail message : messages) {
                statement.setString(1, message.id());
                statement.setString(2, message.from());
                statement.setString(3, message.subject());
                if (message.receivedAt() != null) {
                    statement.setTimestamp(4, Timestamp.from(message.receivedAt().toInstant()));
                } else {
                    statement.setNull(4, java.sql.Types.TIMESTAMP);
                }
                statement.setBoolean(5, message.unread());
                if (message.rawMessage() != null) {
                    statement.setBytes(6, message.rawMessage());
                } else {
                    statement.setNull(6, java.sql.Types.BINARY);
                }
                statement.addBatch();
            }

            statement.executeBatch();
            connection.commit();
            log.debug("Upserted {} email messages", messages.size());
        } catch (SQLException ex) {
            log.error("Failed to persist offline email cache", ex);
        }
    }

    public List<EmailPreview> loadPreviews() {
        List<EmailPreview> results = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, sender, subject, received_at, unread FROM email_messages WHERE trashed = FALSE ORDER BY received_at DESC NULLS LAST, id DESC LIMIT ?")) {

            statement.setInt(1, previewLimit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String sender = resultSet.getString("sender");
                    String subject = resultSet.getString("subject");
                    Timestamp receivedAt = resultSet.getTimestamp("received_at");
                    boolean unread = resultSet.getBoolean("unread");

                    OffsetDateTime timestamp = null;
                    if (receivedAt != null) {
                        timestamp = receivedAt.toInstant().atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                    }

                    results.add(new EmailPreview(id, sender, subject, timestamp, unread));
                }
            }
        } catch (SQLException ex) {
            log.error("Failed to read offline email cache", ex);
        }

        return results;
    }

    public List<EmailPreview> loadTrashPreviews() {
        List<EmailPreview> results = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, sender, subject, received_at, unread FROM email_messages WHERE trashed = TRUE ORDER BY received_at DESC NULLS LAST, id DESC LIMIT ?")) {

            statement.setInt(1, previewLimit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String sender = resultSet.getString("sender");
                    String subject = resultSet.getString("subject");
                    Timestamp receivedAt = resultSet.getTimestamp("received_at");
                    boolean unread = resultSet.getBoolean("unread");

                    OffsetDateTime timestamp = null;
                    if (receivedAt != null) {
                        timestamp = receivedAt.toInstant().atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                    }

                    results.add(new EmailPreview(id, sender, subject, timestamp, unread));
                }
            }
        } catch (SQLException ex) {
            log.error("Failed to read trash email cache", ex);
        }

        return results;
    }

    public boolean markAsTrashed(String id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE email_messages SET trashed = TRUE WHERE id = ?")) {

            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();
            log.debug("Marked email {} as trashed, rows affected: {}", id, rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException ex) {
            log.error("Failed to mark email as trashed: id={}", id, ex);
            return false;
        }
    }

    public boolean unmarkAsTrashed(String id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE email_messages SET trashed = FALSE WHERE id = ?")) {

            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();
            log.debug("Reverted trashed flag for email {}, rows affected: {}", id, rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException ex) {
            log.error("Failed to revert trashed flag for email: id={}", id, ex);
            return false;
        }
    }

    public boolean permanentlyDelete(String id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM email_messages WHERE id = ?")) {

            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();
            log.debug("Permanently deleted email {}, rows affected: {}", id, rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException ex) {
            log.error("Failed to permanently delete email: id={}", id, ex);
            return false;
        }
    }

    public boolean updateMessageId(String oldId, String newId) {
        if (Objects.equals(oldId, newId)) {
            return true;
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE email_messages SET id = ? WHERE id = ?")) {

            statement.setString(1, newId);
            statement.setString(2, oldId);
            int rowsAffected = statement.executeUpdate();
            log.debug("Updated email id from {} to {}, rows affected: {}", oldId, newId, rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException ex) {
            log.error("Failed to update email id from {} to {}", oldId, newId, ex);
            return false;
        }
    }

    public boolean markAsRead(String id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE email_messages SET unread = FALSE WHERE id = ?")) {

            statement.setString(1, id);
            int rowsAffected = statement.executeUpdate();
            log.debug("Marked email {} as read, rows affected: {}", id, rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException ex) {
            log.error("Failed to mark email as read: id={}", id, ex);
            return false;
        }
    }

    public boolean isTrashed(String id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT trashed FROM email_messages WHERE id = ?")) {

            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("trashed");
                }
            }
        } catch (SQLException ex) {
            log.error("Failed to check if email is trashed: id={}", id, ex);
        }
        return false;
    }

    public Optional<StoredEmail> loadEmailById(String id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, sender, subject, received_at, unread, raw FROM email_messages WHERE id = ?")) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String sender = resultSet.getString("sender");
                    String subject = resultSet.getString("subject");
                    Timestamp receivedAt = resultSet.getTimestamp("received_at");
                    boolean unread = resultSet.getBoolean("unread");
                    byte[] raw = resultSet.getBytes("raw");

                    OffsetDateTime timestamp = null;
                    if (receivedAt != null) {
                        timestamp = receivedAt.toInstant().atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                    }

                    return Optional.of(new StoredEmail(id, sender, subject, timestamp, unread, raw));
                }
            }
        } catch (SQLException ex) {
            log.error("Failed to load email by id={}", id, ex);
        }

        return Optional.empty();
    }

    public record StoredEmail(String id,
                              String from,
                              String subject,
                              OffsetDateTime receivedAt,
                              boolean unread,
                              byte[] rawMessage) {

        public StoredEmail {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(from, "from");
        }

        public EmailPreview toPreview() {
            return new EmailPreview(id, from, subject, receivedAt, unread);
        }

        public InputStream rawMessageStream() {
            return rawMessage == null ? InputStream.nullInputStream() : new ByteArrayInputStream(rawMessage);
        }
    }
}

