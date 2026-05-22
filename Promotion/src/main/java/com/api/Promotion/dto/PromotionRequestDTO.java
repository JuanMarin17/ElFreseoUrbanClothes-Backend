package com.api.Promotion.dto;

import java.math.BigDecimal;

import com.api.Promotion.enums.DiscountType;

import lombok.Data;

@Data
public class PromotionRequestDTO {
    private String name;
    private BigDecimal discount;
    private DiscountType discountType;
}