package com.api.OrderPayment.controller;

import com.api.OrderPayment.dto.payment.PaymentResponseDTO;
import com.api.OrderPayment.dto.payment.ProcessPaymentRequestDTO;
import com.api.OrderPayment.dto.payment.RefundRequestDTO;
import com.api.OrderPayment.service.PaymentService;
import com.api.OrderPayment.util.HeaderUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Gestión de pagos asociados a una orden.
 *
 * El userId se lee del header "x-user-id" (UUID).
 *
 * POST  /api/stores/{storeId}/orders/{orderId}/payment         → Procesar pago
 * GET   /api/stores/{storeId}/orders/{orderId}/payment         → Ver detalle del pago
 * POST  /api/stores/{storeId}/orders/{orderId}/payment/refund  → Solicitar reembolso
 */
@RestController
@RequestMapping("/api/stores/{storeId}/orders/{orderId}/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final HeaderUtil headerUtil;

    /** Procesar el pago de una orden */
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId,
            @Valid @RequestBody ProcessPaymentRequestDTO dto) {

        UUID userId = requireUserId();
        return ResponseEntity.ok(paymentService.processPayment(orderId, userId, dto));
    }

    /** Ver el detalle del pago de una orden */
    @GetMapping
    public ResponseEntity<PaymentResponseDTO> getPayment(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId) {

        UUID userId = requireUserId();
        return ResponseEntity.ok(paymentService.getPaymentByOrder(orderId, userId));
    }

    /** Solicitar reembolso de un pago aprobado */
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponseDTO> refundPayment(
            @PathVariable UUID storeId,
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) RefundRequestDTO dto) {

        UUID userId = requireUserId();
        if (dto == null) dto = new RefundRequestDTO();
        return ResponseEntity.ok(paymentService.refundPayment(orderId, userId, dto));
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private UUID requireUserId() {
        return headerUtil.getUserIdFromHeader()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El header 'x-user-id' es obligatorio"));
    }
}
