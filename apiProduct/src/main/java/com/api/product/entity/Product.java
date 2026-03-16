package com.api.product.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Long id_product;

    @Column(name = "name_product")
    private String nameProduct;

    @Column(name = "description_product")
    private String description_product;

    @Column(name = "size_product")
    private String size_product;

    @Column(name = "sale_price_product")
    private Double sale_price_product;

    @Column(name = "supplier_price_product")
    private Double supplier_price_product;

    @Column(name = "stock_product")
    private Long stock_product;


    @Column(name = "category_id ")
    private Long id_category;

    @Column(name = "state_product")
    private Boolean state_product;

    @Column(name = "image_url")
    private String image_url;


}

