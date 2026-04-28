package com.api.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.api.product.entity.Brand;

import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
}