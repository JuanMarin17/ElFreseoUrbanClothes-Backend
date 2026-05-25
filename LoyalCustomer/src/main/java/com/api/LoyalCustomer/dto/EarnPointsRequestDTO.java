package com.api.LoyalCustomer.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class EarnPointsRequestDTO {
    private UUID userId;
    private UUID storeId;
    private BigDecimal orderTotal;
}