package com.api.Cms.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class ProductCardDTO {
    private UUID productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String storeId;
    private String category;
}