package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.id = :id")
    Optional<Card> findWithLockById(@Param("id") UUID id);

    @Query("""
            select c
            from Card c
            where (:ownerId is null or c.owner.id = :ownerId)
              and (:status is null or c.status = :status)
              and (:last4 is null or c.last4 = :last4)
            """)
    Page<Card> searchCards(
            @Param("ownerId") UUID ownerId,
            @Param("status") CardStatus status,
            @Param("last4") String last4,
            Pageable pageable
    );
}

