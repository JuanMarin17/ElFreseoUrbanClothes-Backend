package com.api.product.dto;

import lombok.Data;

@Data
public class ProductResponseDTO {
    private Long id_product;
    private String name_product;
    private String description_product;
    private String size_product;
    private Double sale_price_product;
    private Double supplier_price_product;
    private Long stock_product;
    private Long id_category;
    private Long state_product;
}
