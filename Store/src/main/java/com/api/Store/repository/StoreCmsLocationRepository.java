package com.api.Store.repository;

import com.api.Store.entity.StoreCmsLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface StoreCmsLocationRepository extends JpaRepository<StoreCmsLocation, UUID> {

    List<StoreCmsLocation> findByStoreIdOrderBySortOrderAsc(UUID storeId);

    @Modifying
    @Query("DELETE FROM StoreCmsLocation l WHERE l.storeId = :storeId")
    void deleteByStoreId(UUID storeId);
}
