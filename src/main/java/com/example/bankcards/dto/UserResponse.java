package com.example.bankcards.dto;

import com.example.bankcards.entity.RoleName;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        Set<RoleName> roles
) {
}

