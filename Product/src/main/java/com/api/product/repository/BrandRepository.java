package com.api.product.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.product.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
    List<Brand> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}