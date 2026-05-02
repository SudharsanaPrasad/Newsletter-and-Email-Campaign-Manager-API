package com.example.newsletter.dto;

import com.example.newsletter.entity.CampaignStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CampaignRequest(

        @NotBlank(message = "name is required")
        @Size(max = 255, message = "name must be at most 255 characters")
        String name,

        @NotBlank(message = "subject is required")
        @Size(max = 255, message = "subject must be at most 255 characters")
        String subject,

        @NotBlank(message = "content is required")
        @Size(max = 5000, message = "content must be at most 5000 characters")
        String content,

        @NotNull(message = "mailingListId is required")
        Long mailingListId,

        // optional: DRAFT (default) or SCHEDULED. scheduledTime is required when SCHEDULED.
        CampaignStatus status,

        LocalDateTime scheduledTime
) {
}
