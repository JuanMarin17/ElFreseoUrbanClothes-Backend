package com.api.Inventory.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Inventory.entity.InventoryMovement;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {
    List<InventoryMovement> findByStoreId(UUID storeId);

    List<InventoryMovement> findByVariantIdAndStoreId(UUID variantId, UUID storeId);
}