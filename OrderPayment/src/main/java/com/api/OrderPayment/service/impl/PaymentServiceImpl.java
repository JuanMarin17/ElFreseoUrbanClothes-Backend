package com.api.OrderPayment.service.impl;

import com.api.OrderPayment.dto.payment.PaymentResponseDTO;
import com.api.OrderPayment.dto.payment.ProcessPaymentRequestDTO;
import com.api.OrderPayment.dto.payment.RefundRequestDTO;
import com.api.OrderPayment.entity.Order;
import com.api.OrderPayment.entity.Payment;
import com.api.OrderPayment.enums.OrderStatus;
import com.api.OrderPayment.enums.PaymentStatus;
import com.api.OrderPayment.exception.OrderNotFoundException;
import com.api.OrderPayment.exception.PaymentException;
import com.api.OrderPayment.repository.OrderRepository;
import com.api.OrderPayment.repository.PaymentRepository;
import com.api.OrderPayment.service.PaymentService;
import com.api.OrderPayment.util.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public PaymentResponseDTO processPayment(UUID orderId, UUID userId, ProcessPaymentRequestDTO dto) {
        log.info("Procesando pago para orden: {}", orderId);

        Order order = findOrderForUser(orderId, userId);

        // Validar estado de la orden
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new PaymentException(
                    "Solo se pueden pagar órdenes en estado PENDING. Estado actual: " + order.getStatus());
        }

        // Validar que no tenga un pago previo aprobado
        paymentRepository.findByOrderId(orderId).ifPresent(existingPayment -> {
            if (existingPayment.getStatus() == PaymentStatus.APPROVED) {
                throw new PaymentException("Esta orden ya tiene un pago aprobado.");
            }
        });

        /*
         * INTEGRACIÓN CON GATEWAY DE PAGO
         * ─────────────────────────────────
         * En producción, aquí se llamaría al gateway externo (Stripe, PayU, MercadoPago, etc.)
         * usando el transactionReference que envió el frontend (token del cliente).
         *
         * Ejemplo con Stripe:
         *   PaymentIntent intent = stripe.paymentIntents.confirm(dto.getTransactionReference());
         *   boolean approved = "succeeded".equals(intent.getStatus());
         *
         * Por ahora, si viene transactionReference → APPROVED, si no → REJECTED (simulación).
         */
        boolean paymentApproved = dto.getTransactionReference() != null
                && !dto.getTransactionReference().isBlank();

        PaymentStatus paymentStatus = paymentApproved ? PaymentStatus.APPROVED : PaymentStatus.REJECTED;

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotal())
                .method(dto.getMethod())
                .status(paymentStatus)
                .transactionReference(dto.getTransactionReference())
                .details(dto.getDetails())
                .paidAt(paymentApproved ? LocalDateTime.now() : null)
                .build();

        paymentRepository.save(payment);

        // Actualizar estado de la orden según resultado del pago
        if (paymentApproved) {
            order.setStatus(OrderStatus.CONFIRMED);
            log.info("Pago aprobado para orden: {}", order.getOrderNumber());
        } else {
            log.warn("Pago rechazado para orden: {}", order.getOrderNumber());
        }

        orderRepository.save(order);

        return orderMapper.toPaymentDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByOrder(UUID orderId, UUID userId) {
        findOrderForUser(orderId, userId); // valida que la orden pertenece al usuario

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException(
                        "No se encontró pago para la orden: " + orderId));

        return orderMapper.toPaymentDTO(payment);
    }

    @Override
    @Transactional
    public PaymentResponseDTO refundPayment(UUID orderId, UUID userId, RefundRequestDTO dto) {
        log.info("Solicitando reembolso para orden: {}", orderId);

        Order order = findOrderForUser(orderId, userId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException(
                        "No se encontró pago para la orden: " + orderId));

        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new PaymentException(
                    "Solo se pueden reembolsar pagos aprobados. Estado actual: " + payment.getStatus());
        }

        /*
         * INTEGRACIÓN CON GATEWAY DE PAGO – REEMBOLSO
         * ─────────────────────────────────────────────
         * En producción, aquí se llamaría al gateway para hacer el refund:
         *   stripe.refunds.create({ payment_intent: payment.getTransactionReference() });
         */

        payment.setStatus(PaymentStatus.REFUNDED);
        order.setStatus(OrderStatus.REFUNDED);

        paymentRepository.save(payment);
        orderRepository.save(order);

        log.info("Reembolso procesado para orden: {}. Motivo: {}", order.getOrderNumber(), dto.getReason());

        return orderMapper.toPaymentDTO(payment);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Order findOrderForUser(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException("Orden no encontrada: " + orderId);
        }
        return order;
    }
}
