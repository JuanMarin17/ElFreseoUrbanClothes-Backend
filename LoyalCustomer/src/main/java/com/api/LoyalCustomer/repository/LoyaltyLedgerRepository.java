package com.api.LoyalCustomer.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.api.LoyalCustomer.entity.LoyaltyLedger;

public interface LoyaltyLedgerRepository extends JpaRepository<LoyaltyLedger, UUID> {
    List<LoyaltyLedger> findByAccountId(UUID accountId);

    // Puntos EARN que ya expiraron y no han sido procesados
    @Query("SELECT l FROM LoyaltyLedger l WHERE l.type = 'EARN' AND l.expiresAt < :now AND l.accountId = :accountId")
    List<LoyaltyLedger> findExpiredEarnsByAccountId(UUID accountId, OffsetDateTime now);
}