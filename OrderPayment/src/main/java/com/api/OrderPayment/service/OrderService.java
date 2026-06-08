package com.api.OrderPayment.service;

import com.api.OrderPayment.client.CartClient;
import com.api.OrderPayment.client.dto.CartResponseDTO;
import com.api.OrderPayment.dto.order.CreateOrderRequestDTO;
import com.api.OrderPayment.dto.order.OrderResponseDTO;
import com.api.OrderPayment.dto.order.UpdateOrderStatusRequestDTO;
import com.api.OrderPayment.entity.Order;
import com.api.OrderPayment.entity.OrderItem;
import com.api.OrderPayment.enums.OrderStatus;
import com.api.OrderPayment.exception.OrderNotFoundException;
import com.api.OrderPayment.repository.OrderRepository;
import com.api.OrderPayment.util.OrderMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final OrderMapper orderMapper;

    private final AtomicLong orderCounter = new AtomicLong(1);

    @PostConstruct
    void initOrderCounter() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long ordensHoy = orderRepository.countByCreatedAtGreaterThanEqual(startOfDay);
        orderCounter.set(ordensHoy + 1);
    }

    @Transactional
    public OrderResponseDTO createOrderFromCart(UUID storeId, UUID userId, CreateOrderRequestDTO dto) {
        log.info("Creando orden desde carrito: storeId={}, userId={}", storeId, userId);

        CartResponseDTO cart = cartClient.getCart(storeId, userId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío. Agrega productos antes de crear una orden.");
        }

        Order order = Order.builder()
                .userId(userId)
                .storeId(storeId)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .subtotal(cart.getTotal())
                .tax(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .total(cart.getTotal())
                .shippingAddress(dto.getShippingAddress())
                .notes(dto.getNotes())
                .build();

        List<OrderItem> items = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getUnitPrice())
                        .subtotal(cartItem.getSubtotal())
                        .build())
                .toList();

        order.getItems().addAll(items);

        Order saved = orderRepository.save(order);
        log.info("Orden creada exitosamente: orderNumber={}", saved.getOrderNumber());

        try {
            cartClient.clearCart(storeId, userId);
            log.info("Carrito vaciado tras crear orden: storeId={}, userId={}", storeId, userId);
        } catch (Exception e) {
            log.warn("No se pudo vaciar el carrito automáticamente. Orden creada: {}. Error: {}",
                    saved.getOrderNumber(), e.getMessage());
        }

        return orderMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(UUID orderId, UUID userId) {
        return orderMapper.toDTO(findOrderForUser(orderId, userId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUser(UUID storeId, UUID userId) {
        return orderRepository.findByUserIdAndStoreIdOrderByCreatedAtDesc(userId, storeId)
                .stream()
                .map(orderMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStore(UUID storeId) {
        return orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(orderMapper::toDTO)
                .toList();
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(UUID orderId, UpdateOrderStatusRequestDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada: " + orderId));

        log.info("Actualizando estado de orden {}: {} → {}", order.getOrderNumber(), order.getStatus(), dto.getStatus());
        order.setStatus(dto.getStatus());
        return orderMapper.toDTO(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDTO cancelOrder(UUID orderId, UUID userId) {
        Order order = findOrderForUser(orderId, userId);

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Solo se pueden cancelar órdenes en estado PENDING o CONFIRMED. Estado actual: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        log.info("Orden cancelada: {}", order.getOrderNumber());
        return orderMapper.toDTO(orderRepository.save(order));
    }

    public Order findOrderForUser(UUID orderId, UUID userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada: " + orderId));
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%06d", orderCounter.getAndIncrement());
        String candidate = "ORD-" + date + "-" + seq;

        while (orderRepository.existsByOrderNumber(candidate)) {
            seq = String.format("%06d", orderCounter.getAndIncrement());
            candidate = "ORD-" + date + "-" + seq;
        }
        return candidate;
    }
}
