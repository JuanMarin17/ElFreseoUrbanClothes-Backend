package com.api.Cart.repository;

import com.api.Cart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByProductIdAndStoreId(UUID productId, UUID storeId);
}
