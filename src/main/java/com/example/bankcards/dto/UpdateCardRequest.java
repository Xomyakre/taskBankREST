package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record UpdateCardRequest(
        @NotBlank
        @Pattern(regexp = "^(19\\d{2}|20\\d{2})-(0[1-9]|1[0-2])$", message = "expiryMonth must be YYYY-MM")
        String expiryMonth,
        @NotNull
        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal balance,
        @NotNull CardStatus status
) {
}

