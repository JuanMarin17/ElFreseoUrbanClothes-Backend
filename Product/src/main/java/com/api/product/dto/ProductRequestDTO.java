package com.api.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    private String name;
    private String description;
    private UUID brandId;

    private List<VariantDTO> variants;
    private List<String> images;
    private List<UUID> categoryIds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VariantDTO {
        private String sku;
        private BigDecimal price;
        private Integer stock;
        private Integer minStock;
    }

}