package com.api.Transaction.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.api.Transaction.enums.SubscriptionStatus;

import lombok.Data;

@Data
public class SubscriptionResponseDTO {
    private UUID subscriptionId;
    private UUID storeId;
    private String planName;
    private SubscriptionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime renewalAt;
}