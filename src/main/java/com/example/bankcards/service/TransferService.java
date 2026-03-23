package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.CardTransfer;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardTransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class TransferService {

    private final CardRepository cardRepository;
    private final CardTransferRepository cardTransferRepository;

    public TransferService(CardRepository cardRepository, CardTransferRepository cardTransferRepository) {
        this.cardRepository = cardRepository;
        this.cardTransferRepository = cardTransferRepository;
    }

    @Transactional
    public TransferResponse transfer(UUID ownerId, UUID fromCardId, TransferRequest request) {
        UUID toCardId = request.toCardId();
        BigDecimal amount = request.amount();

        if (fromCardId.equals(toCardId)) {
            throw new BadRequestException("Cannot transfer to the same card");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive");
        }

        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);

        // Захват блокировок в согласованном порядке для предотвращения дедлоков
        UUID firstId = fromCardId.compareTo(toCardId) < 0 ? fromCardId : toCardId;
        UUID secondId = fromCardId.compareTo(toCardId) < 0 ? toCardId : fromCardId;

        Card first = cardRepository.findWithLockById(firstId)
                .orElseThrow(() -> new NotFoundException("Card not found: " + firstId));
        Card second = cardRepository.findWithLockById(secondId)
                .orElseThrow(() -> new NotFoundException("Card not found: " + secondId));

        Card from = fromCardId.equals(firstId) ? first : second;
        Card to = toCardId.equals(firstId) ? first : second;

        if (!from.getOwner().getId().equals(ownerId) || !to.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Transfers are allowed only between your own cards");
        }

        if (resolvedStatus(from) != CardStatus.ACTIVE || resolvedStatus(to) != CardStatus.ACTIVE) {
            throw new BadRequestException("Both cards must be active for transfer");
        }

        BigDecimal fromBalance = from.getBalance().setScale(2, RoundingMode.HALF_UP);
        if (fromBalance.compareTo(normalizedAmount) < 0) {
            throw new BadRequestException("Insufficient funds");
        }

        from.setBalance(fromBalance.subtract(normalizedAmount));
        to.setBalance(to.getBalance().setScale(2, RoundingMode.HALF_UP).add(normalizedAmount));

        CardTransfer transfer = new CardTransfer();
        transfer.setFromCard(from);
        transfer.setToCard(to);
        transfer.setAmount(normalizedAmount);
        transfer.setCreatedAt(Instant.now());
        cardTransferRepository.save(transfer);

        return new TransferResponse(transfer.getId(), from.getId(), to.getId(), normalizedAmount, transfer.getCreatedAt());
    }

    private CardStatus resolvedStatus(Card card) {
        return card.getExpiryDate().isBefore(LocalDate.now()) ? CardStatus.EXPIRED : card.getStatus();
    }
}

