package com.api.PosSale.repository;

import com.api.PosSale.entity.PosSale;
import com.api.PosSale.enums.PosSaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PosSaleRepository extends JpaRepository<PosSale, UUID> {

    Page<PosSale> findByStoreIdOrderByCreatedAtDesc(UUID storeId, Pageable pageable);

    Page<PosSale> findByStoreIdAndStatusOrderByCreatedAtDesc(UUID storeId, PosSaleStatus status, Pageable pageable);

    Optional<PosSale> findBySaleIdAndStoreId(UUID saleId, UUID storeId);

    Page<PosSale> findByStoreIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID storeId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<PosSale> findByStoreIdAndCustomerIdOrderByCreatedAtDesc(UUID storeId, UUID customerId, Pageable pageable);

    boolean existsBySaleNumber(String saleNumber);

    long countByStoreIdAndCreatedAtGreaterThanEqual(UUID storeId, LocalDateTime from);

    @Query("SELECT COUNT(s) FROM PosSale s WHERE s.storeId = :storeId AND s.status = :status AND s.createdAt >= :from")
    long countByStoreIdAndStatusAndCreatedAtGreaterThanEqual(UUID storeId, PosSaleStatus status, LocalDateTime from);
}
