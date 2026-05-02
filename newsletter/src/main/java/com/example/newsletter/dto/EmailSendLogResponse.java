package com.example.newsletter.dto;

import java.time.LocalDateTime;

public record EmailSendLogResponse(
        Long id,
        String recipientName,
        String recipientEmail,
        LocalDateTime sentAt,
        String status
) {
}
