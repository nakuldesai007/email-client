package com.emailclient.backend.email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "email-client")
public class EmailClientProperties {

    private final Imap imap = new Imap();
    private final Storage storage = new Storage();
    private final Crypto crypto = new Crypto();
    private final Smtp smtp = new Smtp();

    public Imap getImap() {
        return imap;
    }

    public Storage getStorage() {
        return storage;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public Smtp getSmtp() {
        return smtp;
    }

    public static class Imap {

        @NotBlank
        private String host;

        @Positive
        private int port = 993;

        private boolean ssl = true;

        @NotBlank
        private String username;

        @NotBlank
        private String password;

        @Positive
        private int fetchBatchSize = 100;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getFetchBatchSize() {
            return fetchBatchSize;
        }

        public void setFetchBatchSize(int fetchBatchSize) {
            this.fetchBatchSize = fetchBatchSize;
        }
    }

    public static class Storage {

        @Positive
        private int previewLimit = 50;

        public int getPreviewLimit() {
            return previewLimit;
        }

        public void setPreviewLimit(int previewLimit) {
            this.previewLimit = previewLimit;
        }
    }

    public static class Crypto {

        @NotBlank
        @Size(min = 32)
        private String masterKey;

        @NotBlank
        private String salt;

        public String getMasterKey() {
            return masterKey;
        }

        public void setMasterKey(String masterKey) {
            this.masterKey = masterKey;
        }

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }
    }

    public static class Smtp {
        // Legacy fields removed - using plain password from spring.mail.password instead
    }
}

