package com.emailclient.backend.email.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record EmailDetail(
        String id,
        String from,
        String subject,
        String body,
        OffsetDateTime receivedAt,
        boolean unread,
        List<String> to,
        List<String> cc
) {
}

