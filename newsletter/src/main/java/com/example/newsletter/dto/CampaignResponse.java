package com.example.newsletter.dto;

import com.example.newsletter.entity.CampaignStatus;

import java.time.LocalDateTime;

public record CampaignResponse(
        Long id,
        String name,
        String subject,
        String content,
        Long mailingListId,
        String mailingListName,
        CampaignStatus status,
        LocalDateTime scheduledTime,
        LocalDateTime sentTime,
        LocalDateTime createdAt
) {
}
