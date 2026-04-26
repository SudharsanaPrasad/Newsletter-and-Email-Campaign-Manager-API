package com.example.newsletter.dto;

public record SubscriberResponse(
        Long id,
        String name,
        String email
) {
}
