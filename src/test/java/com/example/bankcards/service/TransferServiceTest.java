package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.CardTransfer;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.RoleName;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardTransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    CardRepository cardRepository;

    @Mock
    CardTransferRepository cardTransferRepository;

    @InjectMocks
    TransferService transferService;

    private static User user(UUID id) {
        Role role = new Role(UUID.fromString("00000000-0000-0000-0000-000000000002"), RoleName.USER);
        return new User(id, "u" + id.toString().substring(0, 6), "{noop}pw", Set.of(role));
    }

    private static Card card(UUID id, User owner, CardStatus status, BigDecimal balance) {
        Card c = new Card();
        c.setId(id);
        c.setOwner(owner);
        c.setExpiryDate(LocalDate.now().plusMonths(1));
        c.setStatus(status);
        c.setBalance(balance);
        c.setLast4("1234");
        return c;
    }

    @Test
    void transfer_throws_when_cards_belong_to_different_users() {
        UUID ownerId = UUID.randomUUID();
        UUID otherOwnerId = UUID.randomUUID();
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        User owner = user(ownerId);
        User otherOwner = user(otherOwnerId);

        Card from = card(fromCardId, owner, CardStatus.ACTIVE, new BigDecimal("100.00"));
        Card to = card(toCardId, otherOwner, CardStatus.ACTIVE, new BigDecimal("100.00"));

        when(cardRepository.findWithLockById(fromCardId)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(toCardId)).thenReturn(Optional.of(to));

        assertThrows(ForbiddenException.class, () ->
                transferService.transfer(ownerId, fromCardId, new TransferRequest(toCardId, new BigDecimal("10.00"))));
    }

    @Test
    void transfer_throws_when_insufficient_funds() {
        UUID ownerId = UUID.randomUUID();
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        User owner = user(ownerId);

        Card from = card(fromCardId, owner, CardStatus.ACTIVE, new BigDecimal("5.00"));
        Card to = card(toCardId, owner, CardStatus.ACTIVE, new BigDecimal("100.00"));

        when(cardRepository.findWithLockById(fromCardId)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(toCardId)).thenReturn(Optional.of(to));

        assertThrows(BadRequestException.class, () ->
                transferService.transfer(ownerId, fromCardId, new TransferRequest(toCardId, new BigDecimal("10.00"))));
    }

    @Test
    void transfer_successfully_updates_balances() {
        UUID ownerId = UUID.randomUUID();
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        User owner = user(ownerId);

        Card from = card(fromCardId, owner, CardStatus.ACTIVE, new BigDecimal("100.00"));
        Card to = card(toCardId, owner, CardStatus.ACTIVE, new BigDecimal("50.00"));

        when(cardRepository.findWithLockById(fromCardId)).thenReturn(Optional.of(from));
        when(cardRepository.findWithLockById(toCardId)).thenReturn(Optional.of(to));
        when(cardTransferRepository.save(any(CardTransfer.class))).thenAnswer(invocation -> {
            CardTransfer transfer = invocation.getArgument(0);
            transfer.setId(UUID.randomUUID());
            // createdAt уже выставляется в TransferService
            return transfer;
        });

        TransferResponse response = transferService.transfer(ownerId, fromCardId, new TransferRequest(toCardId, new BigDecimal("10.00")));

        assertNotNull(response.transferId());
        assertEquals(fromCardId, response.fromCardId());
        assertEquals(toCardId, response.toCardId());
        assertEquals(new BigDecimal("10.00"), response.amount());

        assertEquals(new BigDecimal("90.00"), from.getBalance());
        assertEquals(new BigDecimal("60.00"), to.getBalance());

        verify(cardTransferRepository, times(1)).save(any(CardTransfer.class));
    }
}

