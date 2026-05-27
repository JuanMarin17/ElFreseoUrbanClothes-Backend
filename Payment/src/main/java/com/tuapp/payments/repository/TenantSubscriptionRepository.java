package com.tuapp.payments.repository;

import com.tuapp.payments.entity.TenantSubscription;
import com.tuapp.payments.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription, UUID> {

    Optional<TenantSubscription> findByTenantId(String tenantId);

    Optional<TenantSubscription> findByMpSubscriptionId(String mpSubscriptionId);

    List<TenantSubscription> findByStatusAndExpiresAtBefore(PaymentStatus status, LocalDateTime now);
}
