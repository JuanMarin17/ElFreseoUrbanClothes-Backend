package com.tuapp.payments.repository;

import com.tuapp.payments.entity.PaymentTransaction;
import com.tuapp.payments.enums.PaymentStatus;
import com.tuapp.payments.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByMpPaymentId(String mpPaymentId);

    Optional<PaymentTransaction> findByMpPreferenceId(String mpPreferenceId);

    Optional<PaymentTransaction> findByExternalReference(String externalReference);

    Page<PaymentTransaction> findByTenantId(String tenantId, Pageable pageable);

    Page<PaymentTransaction> findByTenantIdAndStatus(String tenantId, PaymentStatus status, Pageable pageable);

    Page<PaymentTransaction> findByTenantIdAndPaymentType(String tenantId, PaymentType paymentType, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.platformFee), 0) FROM PaymentTransaction t " +
           "WHERE t.status = 'APPROVED' AND t.createdAt BETWEEN :from AND :to")
    BigDecimal sumPlatformFeeByPeriod(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM PaymentTransaction t " +
           "WHERE t.tenantId = :tenantId AND t.status = 'APPROVED' AND t.paymentType IN ('STORE_SALE','STORE_SALE_PRO','STORE_SALE_API')")
    BigDecimal sumApprovedSalesByTenant(String tenantId);
}
