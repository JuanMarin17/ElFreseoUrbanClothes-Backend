package com.api.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Consulta si el nombre del producto ya existe ignorando mayusculas
    boolean existsByNameProductIgnoreCase(String name_product);
}
