package com.emailclient.backend.email.smtp;

import com.emailclient.backend.email.dto.SendEmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

@Component
public class SecureSmtpMailer {

    private static final Logger log = LoggerFactory.getLogger(SecureSmtpMailer.class);

    private final JavaMailSenderImpl mailSenderTemplate;
    public SecureSmtpMailer(JavaMailSender mailSender) {
        Objects.requireNonNull(mailSender, "mailSender");
        if (!(mailSender instanceof JavaMailSenderImpl javaMailSender)) {
            throw new IllegalStateException("Expected JavaMailSenderImpl but got %s".formatted(mailSender.getClass()));
        }
        this.mailSenderTemplate = javaMailSender;
    }

    public void send(SendEmailRequest request) {
        validate(request);

        JavaMailSenderImpl mailSender = cloneTemplate();
        // Use the password from the mail sender template (from spring.mail.password)
        mailSender.setPassword(mailSenderTemplate.getPassword());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(mailSender.getUsername());
            helper.setTo(request.to());
            if (!request.cc().isEmpty()) {
                helper.setCc(request.cc().toArray(String[]::new));
            }
            if (!request.bcc().isEmpty()) {
                helper.setBcc(request.bcc().toArray(String[]::new));
            }
            helper.setSubject(request.subject());
            helper.setText(request.body(), false);

            if (!request.attachments().isEmpty()) {
                log.warn("Attachments are not yet supported; ignoring {} attachments", request.attachments().size());
            }

            mailSender.send(mimeMessage);
        } catch (MailAuthenticationException | MailSendException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MailSendException("Unable to send email", ex);
        }
    }

    private void validate(SendEmailRequest request) {
        Objects.requireNonNull(request, "sendEmailRequest");
        if (request.attachments() != null && !request.attachments().isEmpty()) {
            log.warn("Attachments are provided but not handled: {}", request.attachments());
        }
    }

    private JavaMailSenderImpl cloneTemplate() {
        JavaMailSenderImpl clone = new JavaMailSenderImpl();
        clone.setProtocol(mailSenderTemplate.getProtocol());
        clone.setHost(mailSenderTemplate.getHost());
        clone.setPort(mailSenderTemplate.getPort());
        clone.setUsername(mailSenderTemplate.getUsername());
        clone.setDefaultEncoding(mailSenderTemplate.getDefaultEncoding());
        clone.setJavaMailProperties(copyProperties(mailSenderTemplate.getJavaMailProperties()));
        return clone;
    }

    private Properties copyProperties(Properties original) {
        Properties copy = new Properties();
        original.forEach((key, value) -> copy.put(key, value));
        return copy;
    }
}

