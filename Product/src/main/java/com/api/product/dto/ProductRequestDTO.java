package com.api.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

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
    private List<MultipartFile> images;
    private List<UUID> categoryIds;

    @Data
    public static class VariantDTO {
        private String sku;
        private BigDecimal price;
        private Integer stock;
    }

    @Data
    public static class ImageDTO {
        private String url;
    }
} 