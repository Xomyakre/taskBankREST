package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.UpdateCardRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/cards")
public class AdminCardController {

    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CardResponse createCard(@Valid @RequestBody CreateCardRequest request) {
        return cardService.createCard(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<CardResponse> listCards(
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String last4,
            Pageable pageable
    ) {
        return cardService.listCardsForAdmin(ownerId, status, last4, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{cardId}")
    public CardResponse getCard(@PathVariable UUID cardId) {
        return cardService.getCardForAdmin(cardId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{cardId}")
    public CardResponse updateCard(@PathVariable UUID cardId, @Valid @RequestBody UpdateCardRequest request) {
        return cardService.updateCard(cardId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{cardId}/block")
    public CardResponse block(@PathVariable UUID cardId) {
        return cardService.blockCardAsAdmin(cardId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{cardId}/activate")
    public CardResponse activate(@PathVariable UUID cardId) {
        return cardService.activateCardAsAdmin(cardId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cardId}")
    @ResponseStatus(code = org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
    }
}

