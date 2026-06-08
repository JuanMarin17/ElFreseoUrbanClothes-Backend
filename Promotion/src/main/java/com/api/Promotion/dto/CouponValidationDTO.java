package com.api.Promotion.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class CouponValidationDTO {
    private UUID couponId;
    private String code;
    private BigDecimal discount;
    private String discountType;
}
