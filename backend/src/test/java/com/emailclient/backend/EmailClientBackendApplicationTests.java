package com.emailclient.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "grpc.server.port=0",
        "email-client.imap.host=imap.test.example",
        "email-client.imap.port=993",
        "email-client.imap.username=test-imap-user@example.com",
        "email-client.imap.password=test-imap-password",
        "email-client.crypto.master-key=test-master-key",
        "email-client.crypto.salt=test-crypto-salt",
        "spring.mail.username=test-smtp-user@example.com",
        "spring.mail.password=test-smtp-password",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/emailclient",
        "spring.datasource.username=emailclient",
        "spring.datasource.password=test-database-password"
})
class EmailClientBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
