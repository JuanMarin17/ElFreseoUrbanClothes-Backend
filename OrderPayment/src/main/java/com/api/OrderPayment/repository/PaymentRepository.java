package com.api.OrderPayment.repository;

import com.api.OrderPayment.entity.Payment;
import com.api.OrderPayment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByTransactionReference(String transactionReference);
}
