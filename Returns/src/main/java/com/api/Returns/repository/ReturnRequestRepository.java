package com.api.Returns.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Returns.entity.ReturnRequest;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, UUID> {
    List<ReturnRequest> findByUserId(UUID userId);

    List<ReturnRequest> findByStoreId(UUID storeId);

    List<ReturnRequest> findByOrderId(UUID orderId);

    boolean existsByOrderIdAndUserId(UUID orderId, UUID userId);
}