package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.UpdateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardCryptoService;
import com.example.bankcards.util.ExpiryUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserService userService;
    private final CardCryptoService cardCryptoService;

    public CardService(CardRepository cardRepository, UserService userService, CardCryptoService cardCryptoService) {
        this.cardRepository = cardRepository;
        this.userService = userService;
        this.cardCryptoService = cardCryptoService;
    }

    @Transactional
    public CardResponse createCard(CreateCardRequest request) {
        User owner = userService.getUserEntity(request.ownerId());

        String encrypted = cardCryptoService.encrypt(request.cardNumber());
        String last4 = cardCryptoService.extractLast4(request.cardNumber());

        Card card = new Card();
        card.setOwner(owner);
        card.setEncryptedNumber(encrypted);
        card.setLast4(last4);
        card.setExpiryDate(ExpiryUtil.toEndOfMonth(request.expiryMonth()));
        card.setStatus(request.initialStatus());
        card.setBalance(normalizeMoney(request.initialBalance()));

        Card saved = cardRepository.save(card);
        return toResponse(saved);
    }

    @Transactional
    public CardResponse updateCard(UUID cardId, UpdateCardRequest request) {
        Card card = getCardEntity(cardId);

        card.setExpiryDate(ExpiryUtil.toEndOfMonth(request.expiryMonth()));
        card.setBalance(normalizeMoney(request.balance()));
        card.setStatus(request.status());

        return toResponse(card);
    }

    public Page<CardResponse> listCardsForUser(UUID ownerId, CardStatus status, String last4, Pageable pageable) {
        return cardRepository.searchCards(ownerId, status, last4, pageable).map(this::toResponse);
    }

    public Page<CardResponse> listCardsForAdmin(UUID ownerIdOrNull, CardStatus status, String last4, Pageable pageable) {
        return cardRepository.searchCards(ownerIdOrNull, status, last4, pageable).map(this::toResponse);
    }

    public CardResponse getCardForUser(UUID cardId, UUID ownerId) {
        Card card = getCardEntity(cardId);
        if (!card.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Card does not belong to current user");
        }
        return toResponse(card);
    }

    public CardResponse getCardForAdmin(UUID cardId) {
        Card card = getCardEntity(cardId);
        return toResponse(card);
    }

    @Transactional
    public CardResponse blockCardAsAdmin(UUID cardId) {
        Card card = getCardEntity(cardId);
        if (isExpired(card)) {
            card.setStatus(CardStatus.EXPIRED);
            return toResponse(card);
        }
        card.setStatus(CardStatus.BLOCKED);
        return toResponse(card);
    }

    @Transactional
    public CardResponse activateCardAsAdmin(UUID cardId) {
        Card card = getCardEntity(cardId);
        if (isExpired(card)) {
            card.setStatus(CardStatus.EXPIRED);
            return toResponse(card);
        }
        card.setStatus(CardStatus.ACTIVE);
        return toResponse(card);
    }

    @Transactional
    public CardResponse blockCardAsUser(UUID cardId, UUID ownerId) {
        Card card = getCardEntity(cardId);
        if (!card.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Card does not belong to current user");
        }

        if (isExpired(card)) {
            throw new BadRequestException("Cannot block an expired card");
        }
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new BadRequestException("Card is not active");
        }

        card.setStatus(CardStatus.BLOCKED);
        return toResponse(card);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new NotFoundException("Card not found: " + cardId);
        }
        cardRepository.deleteById(cardId);
    }

    public BigDecimal getBalance(UUID cardId, UUID ownerId) {
        Card card = getCardEntity(cardId);
        if (!card.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Card does not belong to current user");
        }
        return normalizeMoney(card.getBalance());
    }

    private Card getCardEntity(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found: " + cardId));
    }

    private boolean isExpired(Card card) {
        return card.getExpiryDate().isBefore(LocalDate.now());
    }

    private CardResponse toResponse(Card card) {
        CardStatus resolved = isExpired(card) ? CardStatus.EXPIRED : card.getStatus();
        return new CardResponse(
                card.getId(),
                cardCryptoService.maskFromLast4(card.getLast4()),
                card.getOwner().getId(),
                card.getOwner().getUsername(),
                card.getExpiryDate(),
                mapStatusToRussian(resolved),
                normalizeMoney(card.getBalance())
        );
    }

    private static String mapStatusToRussian(CardStatus status) {
        return switch (status) {
            case ACTIVE -> "Активна";
            case BLOCKED -> "Заблокирована";
            case EXPIRED -> "Истек срок";
        };
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

