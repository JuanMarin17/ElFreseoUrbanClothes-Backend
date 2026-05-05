package com.api.product.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.product.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
}