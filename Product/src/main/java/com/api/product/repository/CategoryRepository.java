package com.api.product.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.product.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}