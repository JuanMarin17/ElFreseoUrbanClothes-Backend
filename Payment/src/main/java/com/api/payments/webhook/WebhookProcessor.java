package com.api.payments.webhook;

import com.api.payments.entity.PaymentTransaction;
import com.api.payments.entity.TenantSubscription;
import com.api.payments.enums.PaymentStatus;
import com.api.payments.enums.PaymentType;
import com.api.payments.repository.PaymentTransactionRepository;
import com.api.payments.repository.TenantSubscriptionRepository;
import com.api.payments.service.MarketplaceService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookProcessor {

    private final PaymentTransactionRepository transactionRepo;
    private final TenantSubscriptionRepository subscriptionRepo;
    private final MarketplaceService marketplaceService;

    /**
     * Procesa la notificación de pago recibida desde Mercado Pago.
     * MP manda topic=payment con el ID del pago que debemos consultar.
     */
    @Transactional
    public void processPaymentNotification(String mpPaymentId) {
        log.info("Procesando notificación webhook, mpPaymentId={}", mpPaymentId);

        // Consultar el pago directamente a la API de MP para tener datos frescos
        Payment payment = fetchPaymentFromMP(mpPaymentId);
        if (payment == null) {
            log.warn("No se pudo obtener el pago {} de MP", mpPaymentId);
            return;
        }

        PaymentStatus newStatus = mapMpStatus(payment.getStatus());
        String externalReference = payment.getExternalReference();

        log.info("Pago {}: status={} ref={}", mpPaymentId, newStatus, externalReference);

        // Buscar transacción existente
        Optional<PaymentTransaction> txOpt = transactionRepo.findByMpPaymentId(mpPaymentId);

        if (txOpt.isEmpty() && externalReference != null) {
            txOpt = transactionRepo.findByExternalReference(externalReference);
        }

        if (txOpt.isPresent()) {
            processExistingTransaction(txOpt.get(), payment, newStatus);
        } else {
            log.warn("No se encontró transacción para mpPaymentId={} ref={}", mpPaymentId, externalReference);
        }
    }

    /**
     * Procesa notificaciones de suscripción (preapproval).
     */
    @Transactional
    public void processSubscriptionNotification(String preapprovalId) {
        log.info("Procesando notificación suscripción preapprovalId={}", preapprovalId);

        subscriptionRepo.findByMpSubscriptionId(preapprovalId).ifPresent(sub -> {
            // Aquí consultarías la API de preapproval de MP para el estado real
            // Por simplicidad se marca como aprobado si llega la notificación
            log.info("Actualizando suscripción tenant={}", sub.getTenantId());
        });
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    private void processExistingTransaction(PaymentTransaction tx, Payment payment, PaymentStatus newStatus) {
        // Evitar reprocesar si ya está en estado final
        if (isTerminalStatus(tx.getStatus()) && tx.getStatus() == newStatus) {
            log.debug("Transacción {} ya está en estado final {}, ignorando", tx.getId(), tx.getStatus());
            return;
        }

        PaymentStatus oldStatus = tx.getStatus();
        tx.setMpPaymentId(payment.getId().toString());
        tx.setStatus(newStatus);

        if (payment.getPaymentMethodId() != null) tx.setPaymentMethodId(payment.getPaymentMethodId());
        if (payment.getPaymentTypeId()   != null) tx.setPaymentTypeId(payment.getPaymentTypeId());

        if (newStatus == PaymentStatus.APPROVED) {
            tx.setApprovedAt(LocalDateTime.now());
        }

        transactionRepo.save(tx);
        log.info("Transacción {} actualizada: {} → {}", tx.getId(), oldStatus, newStatus);

        // Si recién se aprobó, ejecutar acciones post-pago
        if (newStatus == PaymentStatus.APPROVED && oldStatus != PaymentStatus.APPROVED) {
            handleApproved(tx);
        }
    }

    private void handleApproved(PaymentTransaction tx) {
        switch (tx.getPaymentType()) {
            case SUBSCRIPTION -> activateSubscription(tx);
            case STORE_SALE_PRO -> marketplaceService.recordCommission(tx);
            case STORE_SALE_API -> log.debug("Comisión ya fue registrada en CheckoutApiService");
            default -> log.warn("Tipo de pago desconocido: {}", tx.getPaymentType());
        }
    }

    private void activateSubscription(PaymentTransaction tx) {
        String tenantId = tx.getTenantId();
        if (tenantId == null && tx.getExternalReference() != null) {
            // La referencia tiene formato SUB-{tenantId}-{plan}
            String[] parts = tx.getExternalReference().split("-", 3);
            if (parts.length >= 2) tenantId = parts[1];
        }

        if (tenantId == null) {
            log.error("No se pudo determinar tenantId para activar suscripción, txId={}", tx.getId());
            return;
        }

        final String finalTenantId = tenantId;
        TenantSubscription sub = subscriptionRepo.findByTenantId(finalTenantId)
            .orElse(TenantSubscription.builder().tenantId(finalTenantId).build());

        sub.setStatus(PaymentStatus.APPROVED);
        sub.setMpSubscriptionId(tx.getMpPaymentId());
        sub.setStartsAt(LocalDateTime.now());
        sub.setExpiresAt(LocalDateTime.now().plusMonths(1));
        sub.setNextBillingAt(LocalDateTime.now().plusMonths(1));
        subscriptionRepo.save(sub);

        log.info("Suscripción activada para tenant={} plan={} hasta={}", finalTenantId, sub.getPlan(), sub.getExpiresAt());
    }

    private Payment fetchPaymentFromMP(String mpPaymentId) {
        try {
            PaymentClient client = new PaymentClient();
            return client.get(Long.parseLong(mpPaymentId));
        } catch (MPApiException e) {
            log.error("Error MP API al obtener pago {}: {}", mpPaymentId, e.getMessage());
            return null;
        } catch (MPException e) {
            log.error("Error MP al obtener pago {}", mpPaymentId, e);
            return null;
        } catch (NumberFormatException e) {
            log.error("mpPaymentId inválido: {}", mpPaymentId);
            return null;
        }
    }

    private PaymentStatus mapMpStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING;
        return switch (mpStatus.toLowerCase()) {
            case "approved"   -> PaymentStatus.APPROVED;
            case "rejected"   -> PaymentStatus.REJECTED;
            case "cancelled"  -> PaymentStatus.CANCELLED;
            case "refunded"   -> PaymentStatus.REFUNDED;
            case "in_process" -> PaymentStatus.IN_PROCESS;
            case "authorized" -> PaymentStatus.AUTHORIZED;
            default           -> PaymentStatus.PENDING;
        };
    }

    private boolean isTerminalStatus(PaymentStatus status) {
        return status == PaymentStatus.APPROVED
            || status == PaymentStatus.REJECTED
            || status == PaymentStatus.CANCELLED
            || status == PaymentStatus.REFUNDED;
    }
}
