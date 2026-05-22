package com.api.product.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.product.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByActiveTrue();

    List<Category> findByStoreIdAndActiveTrue(UUID storeId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndStoreId(String name, UUID storeId);
}