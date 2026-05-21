package com.api.Inventory.dto;

import java.util.UUID;

import com.api.Inventory.enums.MovementType;

import lombok.Data;

@Data
public class MovementRequestDTO {
    private UUID variantId;
    private UUID locationId;
    private Integer quantity;
    private MovementType movementType;
}