package com.api.Transaction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    Optional<Transaction> findByMpPaymentId(String mpPaymentId);
}