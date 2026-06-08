package com.api.Promotion.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class ProductPromotionDTO {
    private UUID promotionId;
    private UUID productId;
    private String name;
    private BigDecimal discount;
    private String discountType;
}
