package com.api.Supplier.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Supplier.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    boolean existsByName(String name);
    Optional<Supplier> findBySupplierIdAndIsActiveTrue(UUID supplierId);
}