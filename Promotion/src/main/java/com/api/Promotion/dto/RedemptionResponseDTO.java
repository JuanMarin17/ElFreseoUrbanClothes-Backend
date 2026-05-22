package com.api.Promotion.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class RedemptionResponseDTO {
    private UUID redemptionId;
    private UUID couponId;
    private UUID userId;
    private OffsetDateTime usedAt;
}