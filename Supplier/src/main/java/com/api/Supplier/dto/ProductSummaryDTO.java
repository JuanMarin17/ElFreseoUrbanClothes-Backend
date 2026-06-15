package com.api.Supplier.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSummaryDTO {
    private UUID productId;
    private String name;
    private String description;
    private String brandName;
    private String status;
    private OffsetDateTime createdAt;
    private List<String> categories;
    private List<VariantSummary> variants;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VariantSummary {
        private UUID variantId;
        private String sku;
        private BigDecimal price;
        private Integer stock;
        private Integer minStock;
        private String size;
        private String color;
    }
}
