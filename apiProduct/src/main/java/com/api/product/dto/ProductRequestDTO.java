package com.api.product.dto;


import lombok.Data;

@Data
public class ProductRequestDTO {

    private String nameProduct;
    private String description_product;
    private String size_product;
    private Double sale_price_product;
    private Double supplier_price_product;
    private Long stock_product;
    private Long id_category;
    private Boolean state_product;
    private org.springframework.web.multipart.MultipartFile image;
}