package com.api.OrderPayment.service;

import com.api.OrderPayment.dto.payment.PaymentResponseDTO;
import com.api.OrderPayment.dto.payment.ProcessPaymentRequestDTO;
import com.api.OrderPayment.dto.payment.RefundRequestDTO;

import java.util.UUID;

public interface PaymentService {

    /**
     * Procesa el pago de una orden existente en estado PENDING.
     * En producción, aquí se integraría con Stripe, PayU, etc.
     */
    PaymentResponseDTO processPayment(UUID orderId, UUID userId, ProcessPaymentRequestDTO dto);

    /** Obtiene el detalle del pago de una orden */
    PaymentResponseDTO getPaymentByOrder(UUID orderId, UUID userId);

    /**
     * Solicita el reembolso de un pago aprobado.
     * Cambia el estado del pago a REFUNDED y la orden a REFUNDED.
     */
    PaymentResponseDTO refundPayment(UUID orderId, UUID userId, RefundRequestDTO dto);
}
