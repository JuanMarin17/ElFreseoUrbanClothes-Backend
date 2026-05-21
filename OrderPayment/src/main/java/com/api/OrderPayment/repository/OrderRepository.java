package com.api.OrderPayment.repository;

import com.api.OrderPayment.entity.Order;
import com.api.OrderPayment.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Order> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    List<Order> findByUserIdAndStoreIdOrderByCreatedAtDesc(UUID userId, UUID storeId);

    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, OrderStatus status);

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);
}
