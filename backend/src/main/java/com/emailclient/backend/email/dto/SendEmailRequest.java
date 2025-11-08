package com.emailclient.backend.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SendEmailRequest(
        @Email @NotBlank String to,
        List<@Email String> cc,
        List<@Email String> bcc,
        @NotBlank String subject,
        @NotBlank String body,
        List<String> attachments
) {

    public SendEmailRequest {
        cc = cc == null ? List.of() : List.copyOf(cc);
        bcc = bcc == null ? List.of() : List.copyOf(bcc);
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }
}

