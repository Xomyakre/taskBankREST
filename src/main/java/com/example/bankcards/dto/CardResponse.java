package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String cardNumberMasked,
        UUID ownerId,
        String ownerUsername,
        LocalDate expiryDate,
        String status,
        BigDecimal balance
) {
}

