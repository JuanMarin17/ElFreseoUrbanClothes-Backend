package com.api.Transaction.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Transaction.entity.StoreSubscription;
import com.api.Transaction.enums.SubscriptionStatus;

public interface StoreSubscriptionRepository extends JpaRepository<StoreSubscription, UUID> {
    Optional<StoreSubscription> findByStoreIdAndStatus(UUID storeId, SubscriptionStatus status);

    Optional<StoreSubscription> findTopByStoreIdOrderByStartedAtDesc(UUID storeId);
}