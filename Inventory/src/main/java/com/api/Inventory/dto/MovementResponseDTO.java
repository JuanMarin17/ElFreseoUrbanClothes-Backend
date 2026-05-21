package com.api.Inventory.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.api.Inventory.enums.MovementType;

import lombok.Data;

@Data
public class MovementResponseDTO {
    private UUID movementId;
    private UUID variantId;
    private UUID storeId;
    private Integer quantity;
    private MovementType movementType;
    private OffsetDateTime createdAt;
}