package com.api.LoyalCustomer.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.api.LoyalCustomer.enums.LedgerType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "loyalty_ledger")
public class LoyaltyLedger {

    @Id
    @GeneratedValue
    @Column(name = "ledger_id")
    private UUID ledgerId;

    @Column(name = "account_id")
    private UUID accountId;

    private Integer points;

    @Enumerated(EnumType.STRING)
    private LedgerType type;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
}