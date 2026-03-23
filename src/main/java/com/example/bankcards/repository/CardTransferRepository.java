package com.example.bankcards.repository;

import com.example.bankcards.entity.CardTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardTransferRepository extends JpaRepository<CardTransfer, UUID> {
}

