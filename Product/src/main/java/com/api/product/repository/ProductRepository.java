package com.api.product.repository;

import com.api.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByNameIgnoreCase(String name);

    // Con storeId
    Page<Product> findByStoreId(UUID storeId, Pageable pageable);

    Page<Product> findByStoreIdAndActiveTrue(UUID storeId, Pageable pageable);

    List<Product> findByStoreId(UUID storeId);

    List<Product> findByStoreIdAndActiveTrue(UUID storeId);

    List<Product> findByStoreIdAndActiveTrueAndCreatedAtAfter(UUID storeId, OffsetDateTime date);

    List<Product> findByStoreIdAndCreatedAtAfter(UUID storeId, OffsetDateTime date);

    boolean existsByNameIgnoreCaseAndStoreId(String name, UUID storeId);

    Page<Product> findByActiveTrue(Pageable pageable);

    List<Product> findByActiveTrue();

    List<Product> findByCreatedAtAfter(OffsetDateTime date);

    List<Product> findByActiveTrueAndCreatedAtAfter(OffsetDateTime date);
}