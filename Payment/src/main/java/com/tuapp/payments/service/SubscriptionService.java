package com.tuapp.payments.service;

import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.tuapp.payments.config.MercadoPagoConfiguration;
import com.tuapp.payments.dto.request.SubscriptionRequest;
import com.tuapp.payments.dto.response.SubscriptionResponse;
import com.tuapp.payments.entity.TenantSubscription;
import com.tuapp.payments.enums.PaymentStatus;
import com.tuapp.payments.exception.PaymentException;
import com.tuapp.payments.repository.TenantSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final TenantSubscriptionRepository subscriptionRepo;
    private final MercadoPagoConfiguration mpConfig;

    /**
     * Crea una preferencia de Checkout Pro para que el tenant pague su plan.
     * El tenant es redirigido a MP, paga, y el webhook activa su suscripción.
     */
    @Transactional
    public SubscriptionResponse createSubscriptionCheckout(SubscriptionRequest request) {
        log.info("Creando checkout de suscripción para tenant={} plan={}", request.getTenantId(), request.getPlan());

        // Si ya tiene una suscripción activa, no crear otra
        subscriptionRepo.findByTenantId(request.getTenantId()).ifPresent(sub -> {
            if (sub.isActive()) {
                throw new PaymentException("El tenant ya tiene una suscripción activa: " + sub.getPlan());
            }
        });

        BigDecimal price = request.getPlan().getPriceInCents();

        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title(request.getPlan().getDisplayName())
                .description("Suscripción mensual — " + request.getPlan().getDisplayName())
                .quantity(1)
                .unitPrice(price)
                .currencyId("COP")
                .build();

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .email(request.getPayerEmail())
                .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(mpConfig.getFrontendUrl() + "/dashboard/subscription/success")
                .failure(mpConfig.getFrontendUrl() + "/dashboard/subscription/failure")
                .pending(mpConfig.getFrontendUrl() + "/dashboard/subscription/pending")
                .build();

            PreferenceRequest preferenceReq = PreferenceRequest.builder()
                .items(List.of(item))
                .payer(payer)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference("SUB-" + request.getTenantId() + "-" + request.getPlan().name())
                .notificationUrl(mpConfig.getNotificationUrl())
                .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceReq);

            // Guardar suscripción en estado PENDING hasta que llegue el webhook
            TenantSubscription subscription = subscriptionRepo.findByTenantId(request.getTenantId())
                .orElse(TenantSubscription.builder().tenantId(request.getTenantId()).build());

            subscription.setPlan(request.getPlan());
            subscription.setStatus(PaymentStatus.PENDING);
            subscription.setMpPreapprovalPlanId(preference.getId());
            subscriptionRepo.save(subscription);

            return SubscriptionResponse.builder()
                .id(subscription.getId())
                .tenantId(request.getTenantId())
                .plan(request.getPlan())
                .status(PaymentStatus.PENDING)
                .checkoutUrl(preference.getInitPoint())
                .sandboxCheckoutUrl(preference.getSandboxInitPoint())
                .build();

        } catch (MPApiException e) {
            log.error("Error MP API al crear suscripción: status={} msg={}", e.getStatusCode(), e.getApiResponse().getContent());
            throw new PaymentException("Error de Mercado Pago: " + e.getMessage(), e);
        } catch (MPException e) {
            log.error("Error MP al crear suscripción", e);
            throw new PaymentException("Error al conectar con Mercado Pago", e);
        }
    }

    /** Consulta el estado de la suscripción de un tenant */
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(String tenantId) {
        TenantSubscription sub = subscriptionRepo.findByTenantId(tenantId)
            .orElseThrow(() -> new PaymentException("No existe suscripción para el tenant: " + tenantId));

        return SubscriptionResponse.builder()
            .id(sub.getId())
            .tenantId(sub.getTenantId())
            .plan(sub.getPlan())
            .status(sub.getStatus())
            .mpSubscriptionId(sub.getMpSubscriptionId())
            .startsAt(sub.getStartsAt())
            .expiresAt(sub.getExpiresAt())
            .nextBillingAt(sub.getNextBillingAt())
            .active(sub.isActive())
            .build();
    }

    /** Verifica si un tenant tiene suscripción activa (para middleware de acceso) */
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(String tenantId) {
        return subscriptionRepo.findByTenantId(tenantId)
            .map(TenantSubscription::isActive)
            .orElse(false);
    }
}
