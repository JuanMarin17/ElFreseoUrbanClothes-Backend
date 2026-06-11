package com.api.product.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
public class ProductResponseDTO {

    private UUID productId;
    private UUID storeId;
    private String name;
    private String description;
    private String brandName;
    private OffsetDateTime createdAt;
    private String status;
    private List<VariantDTO> variants;
    private List<ImageDTO> images;
    private List<String> categories;

    @Data
    @Builder
    public static class VariantDTO {
        private UUID variantId;
        private String sku;
        private BigDecimal price;
        private Integer stock;
        private Integer minStock;
        private String size;
        private String color;
    }

    @Data
    @Builder
    public static class ImageDTO {
        private String url;
    }
}