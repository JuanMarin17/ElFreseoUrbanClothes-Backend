package com.api.Inventory.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Inventory.entity.InventoryBalance;

public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, UUID> {
    List<InventoryBalance> findByStoreId(UUID storeId);

    Optional<InventoryBalance> findByVariantIdAndLocationIdAndStoreId(UUID variantId, UUID locationId, UUID storeId);
}