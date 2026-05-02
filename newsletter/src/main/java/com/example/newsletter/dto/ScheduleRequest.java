package com.example.newsletter.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleRequest(

        @NotNull(message = "scheduledTime is required")
        LocalDateTime scheduledTime
) {
}
