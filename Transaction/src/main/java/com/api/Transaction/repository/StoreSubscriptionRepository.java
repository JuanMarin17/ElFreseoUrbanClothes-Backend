package com.api.Transaction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.api.Transaction.entity.StoreSubscription;
import com.api.Transaction.enums.SubscriptionStatus;

public interface StoreSubscriptionRepository extends JpaRepository<StoreSubscription, UUID> {
    Optional<StoreSubscription> findByStoreIdAndStatus(UUID storeId, SubscriptionStatus status);

    Optional<StoreSubscription> findTopByStoreIdOrderByStartedAtDesc(UUID storeId);

    @Query("SELECT s FROM StoreSubscription s JOIN FETCH s.plan")
    List<StoreSubscription> findAllWithPlan();
}