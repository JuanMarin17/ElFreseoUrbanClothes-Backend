package com.api.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.api.product.entity.Product;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Page<Product> findByActiveTrue(Pageable pageable);

    List<Product> findByCreatedAtAfter(OffsetDateTime date);

    List<Product> findByActiveTrueAndCreatedAtAfter(OffsetDateTime date);
    
    List<Product> findByActiveTrue();


}