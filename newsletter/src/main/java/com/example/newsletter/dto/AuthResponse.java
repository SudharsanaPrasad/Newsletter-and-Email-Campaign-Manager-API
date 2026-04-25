package com.example.newsletter.dto;

import com.example.newsletter.entity.Role;

import java.util.Set;

public record AuthResponse(
        String token,
        String username,
        Set<Role> roles
) {
}
