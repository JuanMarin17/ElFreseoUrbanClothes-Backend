package com.api.Returns.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ReturnItemRequestDTO {
    private UUID variantId;
    private Integer quantity;
}