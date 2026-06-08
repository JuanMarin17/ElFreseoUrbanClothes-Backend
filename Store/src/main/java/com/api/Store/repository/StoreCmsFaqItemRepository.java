package com.api.Store.repository;

import com.api.Store.entity.StoreCmsFaqItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface StoreCmsFaqItemRepository extends JpaRepository<StoreCmsFaqItem, UUID> {

    List<StoreCmsFaqItem> findByStoreIdOrderBySortOrderAsc(UUID storeId);

    @Modifying
    @Query("DELETE FROM StoreCmsFaqItem i WHERE i.storeId = :storeId")
    void deleteByStoreId(UUID storeId);
}
