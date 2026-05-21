package com.api.Supplier.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.Supplier.entity.StoreSupplier;

@Repository
public interface StoreSupplierRepository extends JpaRepository<StoreSupplier, StoreSupplier.StoreSupplierKey> {

    boolean existsByStoreIdAndSupplierId(UUID storeId, UUID supplierId);

    void deleteByStoreIdAndSupplierId(UUID storeId, UUID supplierId);

    @Query("SELECT ss FROM StoreSupplier ss JOIN FETCH ss.supplier WHERE ss.storeId = :storeId")
    List<StoreSupplier> findByStoreIdWithSupplier(@Param("storeId") UUID storeId);
}