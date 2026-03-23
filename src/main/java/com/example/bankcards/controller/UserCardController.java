package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
public class UserCardController {

    private final CardService cardService;
    private final TransferService transferService;

    public UserCardController(CardService cardService, TransferService transferService) {
        this.cardService = cardService;
        this.transferService = transferService;
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public Page<CardResponse> myCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String last4,
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID ownerId = currentUserId(jwt);
        return cardService.listCardsForUser(ownerId, status, last4, pageable);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{cardId}")
    public CardResponse getCard(@PathVariable UUID cardId, @AuthenticationPrincipal Jwt jwt) {
        return cardService.getCardForUser(cardId, currentUserId(jwt));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{cardId}/block")
    public CardResponse block(@PathVariable UUID cardId, @AuthenticationPrincipal Jwt jwt) {
        return cardService.blockCardAsUser(cardId, currentUserId(jwt));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{cardId}/balance")
    public BigDecimal balance(@PathVariable UUID cardId, @AuthenticationPrincipal Jwt jwt) {
        return cardService.getBalance(cardId, currentUserId(jwt));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{fromCardId}/transfers")
    public TransferResponse transfer(
            @PathVariable UUID fromCardId,
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return transferService.transfer(currentUserId(jwt), fromCardId, request);
    }

    private static UUID currentUserId(Jwt jwt) {
        String uid = jwt.getClaimAsString("uid");
        return UUID.fromString(uid);
    }
}

