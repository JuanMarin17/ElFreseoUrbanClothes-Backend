package com.api.payments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.payments.dto.request.CheckoutApiRequest;
import com.api.payments.dto.request.CheckoutProRequest;
import com.api.payments.dto.request.SubscriptionRequest;
import com.api.payments.dto.response.PaymentResponse;
import com.api.payments.dto.response.SubscriptionResponse;
import com.api.payments.service.CheckoutApiService;
import com.api.payments.service.MarketplaceService;
import com.api.payments.service.SubscriptionService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final SubscriptionService subscriptionService;
    private final MarketplaceService marketplaceService;
    private final CheckoutApiService checkoutApiService;

    // ── Suscripciones ────────────────────────────────────────────────────────

    /**
     * POST /api/payments/subscription
     * El tenant elige su plan y obtiene la URL de pago de MP.
     */
    @PostMapping("/subscription")
    public ResponseEntity<SubscriptionResponse> createSubscription(
        @Valid @RequestBody SubscriptionRequest request
    ) {
        return ResponseEntity.ok(subscriptionService.createSubscriptionCheckout(request));
    }

    /**
     * GET /api/payments/subscription/{tenantId}
     * Consulta el estado de la suscripción de un tenant.
     */
    @GetMapping("/subscription/{tenantId}")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable String tenantId) {
        return ResponseEntity.ok(subscriptionService.getSubscription(tenantId));
    }

    /**
     * GET /api/payments/subscription/{tenantId}/active
     * Verifica si un tenant tiene suscripción activa (útil para middleware).
     */
    @GetMapping("/subscription/{tenantId}/active")
    public ResponseEntity<Boolean> isSubscriptionActive(@PathVariable String tenantId) {
        return ResponseEntity.ok(subscriptionService.hasActiveSubscription(tenantId));
    }

    // ── Ventas de tienda — Checkout Pro ─────────────────────────────────────

    /**
     * POST /api/payments/checkout/pro
     * Crea preferencia de Checkout Pro con split automático.
     * El comprador es redirigido a la página de pago de MP.
     */
    @PostMapping("/checkout/pro")
    public ResponseEntity<PaymentResponse> createCheckoutPro(
        @Valid @RequestBody CheckoutProRequest request
    ) {
        return ResponseEntity.ok(marketplaceService.createCheckoutPro(request));
    }

    // ── Ventas de tienda — Checkout API (tarjeta) ────────────────────────────

    /**
     * POST /api/payments/checkout/card
     * Procesa pago con tarjeta tokenizada desde el frontend.
     * El frontend usa MercadoPago.js para tokenizar y manda el cardToken.
     */
    @PostMapping("/checkout/card")
    public ResponseEntity<PaymentResponse> processCardPayment(
        @Valid @RequestBody CheckoutApiRequest request
    ) {
        return ResponseEntity.ok(checkoutApiService.processCardPayment(request));
    }
}
