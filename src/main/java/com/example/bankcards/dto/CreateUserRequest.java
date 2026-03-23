package com.example.bankcards.dto;

import com.example.bankcards.entity.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9_]{3,64}$", message = "username must be 3..64 chars [a-zA-Z0-9_]")
        String username,
        @NotBlank
        @Pattern(regexp = "^.{6,128}$", message = "password must be 6..128 chars")
        String password,
        @NotNull RoleName role
) {
}

