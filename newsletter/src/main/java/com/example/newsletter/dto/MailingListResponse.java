package com.example.newsletter.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MailingListResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        int subscriberCount,
        List<SubscriberResponse> subscribers
) {
}
