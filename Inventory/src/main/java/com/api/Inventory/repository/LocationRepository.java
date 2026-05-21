package com.api.Inventory.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Inventory.entity.Location;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    List<Location> findByStoreId(UUID storeId);
}