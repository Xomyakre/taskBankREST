package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        UUID fromCardId,
        UUID toCardId,
        BigDecimal amount,
        Instant createdAt
) {
}

