package com.api.LoyalCustomer.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class LoyaltyAccountResponseDTO {
    private UUID accountId;
    private UUID userId;
    private UUID storeId;
    private Integer points;
}