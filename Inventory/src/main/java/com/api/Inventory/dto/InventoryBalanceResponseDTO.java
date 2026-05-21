package com.api.Inventory.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class InventoryBalanceResponseDTO {
    private UUID balanceId;
    private UUID variantId;
    private UUID locationId;
    private UUID storeId;
    private Integer quantity;
}