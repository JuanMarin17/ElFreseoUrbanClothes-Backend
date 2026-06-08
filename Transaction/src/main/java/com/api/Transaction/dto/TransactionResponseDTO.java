package com.api.Transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.api.Transaction.enums.TransactionStatus;
import com.api.Transaction.enums.TransactionType;

import lombok.Data;

@Data
public class TransactionResponseDTO {
    private UUID transactionId;
    private UUID storeId;
    private String planName;
    private String mpPaymentId;
    private BigDecimal amount;
    private TransactionStatus status;
    private TransactionType type;
    private LocalDateTime createdAt;
}