package com.api.OrderPayment.repository;

import com.api.OrderPayment.entity.Order;
import com.api.OrderPayment.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** Trae items y payment en la misma consulta para evitar N+1 al recorrer todas las órdenes de la tienda. */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items " +
            "LEFT JOIN FETCH o.payment " +
            "WHERE o.storeId = :storeId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    Page<Order> findByStoreIdOrderByCreatedAtDesc(UUID storeId, Pageable pageable);

    Page<Order> findByStoreIdAndStatusOrderByCreatedAtDesc(UUID storeId, OrderStatus status, Pageable pageable);

    List<Order> findByUserIdAndStoreIdOrderByCreatedAtDesc(UUID userId, UUID storeId);

    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, OrderStatus status);

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    long countByCreatedAtGreaterThanEqual(java.time.LocalDateTime from);
}
