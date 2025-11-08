package com.emailclient.backend.email.dto;

import java.time.OffsetDateTime;

public record EmailPreview(
        String id,
        String from,
        String subject,
        OffsetDateTime receivedAt,
        boolean unread
) {
}

