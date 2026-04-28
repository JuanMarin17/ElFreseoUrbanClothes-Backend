package com.api.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.api.product.entity.Category;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}