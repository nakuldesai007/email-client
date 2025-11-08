package com.emailclient.backend.email.storage;

import com.emailclient.backend.email.EmailClientProperties;
import org.junit.jupiter.api.Test;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailOfflineStoreSchemaTest {

    @Test
    void initializeAddsTrashedColumnForLegacyDatabases() throws Exception {
        Path tempDb = Files.createTempFile("email-client-test", ".db");
        try {
            DataSource dataSource = createLegacyDatabase(tempDb);

            EmailClientProperties properties = new EmailClientProperties();
            EmailOfflineStore store = new EmailOfflineStore(dataSource, properties);

            store.initialize();

            try (Connection connection = dataSource.getConnection();
                 ResultSet columns = connection.getMetaData().getColumns(null, null, "email_messages", "trashed")) {
                assertTrue(columns.next(), "Expected trashed column to be added during initialization");
            }
        } finally {
            Files.deleteIfExists(tempDb);
        }
    }

    private DataSource createLegacyDatabase(Path dbPath) throws Exception {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbPath.toAbsolutePath());

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE email_messages (
                        id TEXT PRIMARY KEY,
                        sender TEXT NOT NULL,
                        subject TEXT,
                        received_at TEXT,
                        unread INTEGER NOT NULL,
                        raw BLOB
                    )
                    """);
        }

        return dataSource;
    }
}

