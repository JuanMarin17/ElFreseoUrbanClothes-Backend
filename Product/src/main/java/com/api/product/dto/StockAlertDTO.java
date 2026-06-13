package com.api.product.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockAlertDTO {

    private UUID storeId;
    private UUID productId;
    private String productName;
    private UUID variantId;
    private String sku;
    private String size;
    private String color;
    private Integer stock;
    private Integer minStock;
    private String message;
    private OffsetDateTime timestamp;
}