package com.api.Supplier.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Supplier.entity.StoreSupplier;

public interface StoreSupplierRepository extends JpaRepository<StoreSupplier, StoreSupplier.StoreSuppliedId> {
    List<StoreSupplier> findByIdStoreId(UUID storeId);

    boolean existsByIdStoreIdAndIdSupplierId(UUID storeId, UUID supplierId);
}