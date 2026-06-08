package com.api.Cart.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ProductResponse {

    private UUID productId;
    private String name;
    private String status;
    private List<VariantInfo> variants;
    private List<ImageInfo> images;

    @Data
    public static class VariantInfo {
        private UUID variantId;
        private String sku;
        private BigDecimal price;
        private Integer stock;
        private String size;
        private String color;
    }

    @Data
    public static class ImageInfo {
        private String url;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public BigDecimal getFirstPrice() {
        return variants != null && !variants.isEmpty() && variants.get(0).getPrice() != null
                ? variants.get(0).getPrice()
                : BigDecimal.ZERO;
    }

    public int getTotalStock() {
        if (variants == null) return 0;
        return variants.stream()
                .mapToInt(v -> v.getStock() != null ? v.getStock() : 0)
                .sum();
    }

    public String getFirstSku() {
        return variants != null && !variants.isEmpty() ? variants.get(0).getSku() : null;
    }

    public String getFirstImageUrl() {
        return images != null && !images.isEmpty() ? images.get(0).getUrl() : null;
    }
}
