package com.api.Supplier.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Supplier.entity.SupplierProduct;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, SupplierProduct.SupplierProductId> {
    List<SupplierProduct> findByIdSupplierIdAndStoreId(UUID supplierId, UUID storeId);
}
