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

    @Column(name = "product_id")
    private Long id_product;

    @Column(name = "name")
    private String name_product;

    @Column(name = "description")
    private String description_product;

    @Column(name = "size")
    private String size_product;

    @Column(name = "sale_price")
    private Double sale_price_product;

    @Column(name = "supplier_price")
    private Double supplier_price_product;

    @Column(name = "stock")
    private Long stock_product;

    // @Column(name = "status")  se debe eliminar el campo status de la base de datos
    // private String status_product;

    @Column(name = "category_id ")
    private Long id_category;

    @Column(name = "state")
    private Long state_product;

}

