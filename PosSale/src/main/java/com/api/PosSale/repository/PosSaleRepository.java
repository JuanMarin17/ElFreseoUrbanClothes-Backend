package com.api.PosSale.repository;

import com.api.PosSale.entity.PosSale;
import com.api.PosSale.enums.PosSaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PosSaleRepository extends JpaRepository<PosSale, UUID> {

    List<PosSale> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    Optional<PosSale> findBySaleIdAndStoreId(UUID saleId, UUID storeId);

    List<PosSale> findByStoreIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID storeId, LocalDateTime from, LocalDateTime to);

    List<PosSale> findByStoreIdAndCustomerIdOrderByCreatedAtDesc(UUID storeId, UUID customerId);

    boolean existsBySaleNumber(String saleNumber);

    long countByCreatedAtGreaterThanEqual(LocalDateTime from);

    @Query("SELECT COUNT(s) FROM PosSale s WHERE s.storeId = :storeId AND s.status = :status AND s.createdAt >= :from")
    long countByStoreIdAndStatusAndCreatedAtGreaterThanEqual(UUID storeId, PosSaleStatus status, LocalDateTime from);
}
