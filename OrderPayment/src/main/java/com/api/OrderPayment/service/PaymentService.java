package com.api.OrderPayment.service;

import com.api.OrderPayment.dto.payment.PaymentResponseDTO;
import com.api.OrderPayment.dto.payment.ProcessPaymentRequestDTO;
import com.api.OrderPayment.dto.payment.RefundRequestDTO;
import com.api.OrderPayment.entity.Order;
import com.api.OrderPayment.entity.Payment;
import com.api.OrderPayment.enums.OrderStatus;
import com.api.OrderPayment.enums.PaymentStatus;
import com.api.OrderPayment.exception.PaymentException;
import com.api.OrderPayment.repository.OrderRepository;
import com.api.OrderPayment.repository.PaymentRepository;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Transactional
    public PaymentResponseDTO processPayment(UUID orderId, UUID userId, ProcessPaymentRequestDTO dto) {
        log.info("Procesando pago para orden: {}", orderId);

        Order order = orderService.findOrderForUser(orderId, userId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new PaymentException(
                    "Solo se pueden pagar órdenes en estado PENDING. Estado actual: " + order.getStatus());
        }

        paymentRepository.findByOrderId(orderId).ifPresent(existingPayment -> {
            if (existingPayment.getStatus() == PaymentStatus.APPROVED) {
                throw new PaymentException("Esta orden ya tiene un pago aprobado.");
            }
        });

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

        if (paymentApproved) {
            order.setStatus(OrderStatus.CONFIRMED);
            log.info("Pago aprobado para orden: {}", order.getOrderNumber());
        } else {
            log.warn("Pago rechazado para orden: {}", order.getOrderNumber());
        }

        orderRepository.save(order);

        return orderMapper.toPaymentDTO(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByOrder(UUID orderId, UUID userId) {
        orderService.findOrderForUser(orderId, userId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException(
                        "No se encontró pago para la orden: " + orderId));

        return orderMapper.toPaymentDTO(payment);
    }

    @Transactional
    public PaymentResponseDTO refundPayment(UUID orderId, UUID userId, RefundRequestDTO dto) {
        log.info("Solicitando reembolso para orden: {}", orderId);

        Order order = orderService.findOrderForUser(orderId, userId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException(
                        "No se encontró pago para la orden: " + orderId));

        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new PaymentException(
                    "Solo se pueden reembolsar pagos aprobados. Estado actual: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        order.setStatus(OrderStatus.REFUNDED);

        paymentRepository.save(payment);
        orderRepository.save(order);

        log.info("Reembolso procesado para orden: {}. Motivo: {}", order.getOrderNumber(), dto.getReason());

        return orderMapper.toPaymentDTO(payment);
    }
}
