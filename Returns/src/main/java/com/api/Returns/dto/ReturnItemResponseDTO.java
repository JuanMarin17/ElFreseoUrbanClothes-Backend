package com.api.Returns.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ReturnItemResponseDTO {
    private UUID returnItemId;
    private UUID variantId;
    private Integer quantity;
    private OffsetDateTime createdAt;
}