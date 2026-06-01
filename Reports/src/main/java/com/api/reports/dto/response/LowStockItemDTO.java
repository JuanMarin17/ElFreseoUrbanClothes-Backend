package com.api.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class LowStockItemDTO {
    private UUID productId;
    private String productName;
    private String sku;
    private int currentStock;
    private int minStock;
    private BigDecimal price;
}
