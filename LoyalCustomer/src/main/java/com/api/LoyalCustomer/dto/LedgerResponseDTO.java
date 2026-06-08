package com.api.LoyalCustomer.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.api.LoyalCustomer.enums.LedgerType;

import lombok.Data;

@Data
public class LedgerResponseDTO {
    private UUID ledgerId;
    private UUID accountId;
    private Integer points;
    private LedgerType type;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
}