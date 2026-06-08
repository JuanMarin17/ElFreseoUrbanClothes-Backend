package com.api.Promotion.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.api.Promotion.enums.DiscountType;

import lombok.Data;

@Data
public class PromotionResponseDTO {
    private UUID promotionId;
    private String name;
    private BigDecimal discount;
    private DiscountType discountType;
    private UUID storeId;
    private UUID productId;
    private Boolean isActive;
    private OffsetDateTime createdAt;
}