package com.emailclient.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "grpc.server.port=0",
        "email-client.imap.host=imap.gmail.com",
        "email-client.imap.port=993",
        "email-client.imap.username=nakuldesai007@gmail.com",
        "email-client.imap.password=aeyuydrkwnzdydvq",
        "email-client.crypto.master-key=I6eTnrSdxciZMsxCbRGIhEW52znNEWtW3tdfJZlKpQQ=",
        "email-client.crypto.salt=0CHc4CDlRF3BY9GYRK0+vg==",
        "spring.mail.username=nakuldesai007@gmail.com",
        "spring.mail.password=aeyuydrkwnzdydvq",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/emailclient",
        "spring.datasource.username=emailclient",
        "spring.datasource.password=DhUzEnqkaXa4ikI533mPPFy5SUo3oD5z"
})
class EmailClientBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}

