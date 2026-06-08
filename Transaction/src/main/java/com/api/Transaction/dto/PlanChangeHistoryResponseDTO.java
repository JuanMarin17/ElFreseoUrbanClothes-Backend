package com.api.Transaction.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.api.Transaction.enums.PlanChangeReason;

import lombok.Data;

@Data
public class PlanChangeHistoryResponseDTO {
    private UUID historyId;
    private UUID storeId;
    private String fromPlan;
    private String toPlan;
    private LocalDateTime changedAt;
    private PlanChangeReason reason;
}