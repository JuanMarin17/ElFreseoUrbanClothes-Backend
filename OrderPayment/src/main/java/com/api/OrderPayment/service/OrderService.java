package com.api.OrderPayment.service;

import com.api.OrderPayment.client.CartClient;
import com.api.OrderPayment.client.PromotionClient;
import com.api.OrderPayment.client.UserClient;
import com.api.OrderPayment.client.dto.CartResponseDTO;
import com.api.OrderPayment.client.dto.CouponValidationDTO;
import com.api.OrderPayment.client.dto.UserInfoDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final PromotionClient promotionClient;
    private final UserClient userClient;
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

        // Base: usa el total con descuentos de producto si está disponible
        BigDecimal baseTotal = cart.getTotalWithDiscount() != null
                ? cart.getTotalWithDiscount()
                : cart.getSubtotal();
        BigDecimal subtotal = cart.getSubtotal() != null ? cart.getSubtotal() : baseTotal;

        // Aplica cupón de descuento si se envió uno
        BigDecimal couponDiscount = BigDecimal.ZERO;
        String appliedCoupon = null;

        if (dto.getCouponCode() != null && !dto.getCouponCode().isBlank()) {
            Optional<CouponValidationDTO> couponOpt = promotionClient.validateCoupon(
                    dto.getCouponCode(), storeId, userId);

            if (couponOpt.isPresent()) {
                CouponValidationDTO coupon = couponOpt.get();
                if ("PERCENTAGE".equals(coupon.getDiscountType())) {
                    couponDiscount = baseTotal.multiply(
                            coupon.getDiscount().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                            .setScale(2, RoundingMode.HALF_UP);
                } else {
                    couponDiscount = coupon.getDiscount().min(baseTotal);
                }
                appliedCoupon = dto.getCouponCode().toUpperCase();
                log.info("Cupón '{}' aplicado: descuento={}", appliedCoupon, couponDiscount);
            } else {
                throw new IllegalArgumentException(
                        "Cupón inválido, inactivo o ya utilizado: " + dto.getCouponCode());
            }
        }

        // Descuento total = descuentos de producto (ya en totalWithDiscount) + cupón
        BigDecimal productDiscount = subtotal.subtract(baseTotal);
        BigDecimal totalDiscount = productDiscount.add(couponDiscount);
        BigDecimal finalTotal = subtotal.subtract(totalDiscount).max(BigDecimal.ZERO);

        Order order = Order.builder()
                .userId(userId)
                .storeId(storeId)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .tax(BigDecimal.ZERO)
                .discount(totalDiscount)
                .total(finalTotal)
                .shippingAddress(dto.getShippingAddress())
                .notes(dto.getNotes())
                .build();

        List<OrderItem> items = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .variantName(cartItem.getVariantName())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getUnitPrice())
                        .subtotal(cartItem.getSubtotal())
                        .build())
                .toList();

        order.getItems().addAll(items);

        Order saved = orderRepository.save(order);
        log.info("Orden creada exitosamente: orderNumber={}", saved.getOrderNumber());

        // Registra redención del cupón después de guardar la orden
        if (appliedCoupon != null) {
            promotionClient.redeemCoupon(appliedCoupon, storeId, userId);
        }

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

    /** Uso interno de otros microservicios (Reports). Sin paginación ni enriquecimiento. */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStoreInternal(UUID storeId) {
        return orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(orderMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByStore(UUID storeId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> ordersPage = (status != null)
                ? orderRepository.findByStoreIdAndStatusOrderByCreatedAtDesc(storeId, status, pageable)
                : orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable);

        // Enriquecer con datos de cliente (una llamada por usuario único)
        Set<UUID> userIds = ordersPage.stream().map(Order::getUserId).collect(Collectors.toSet());
        Map<UUID, UserInfoDTO> userCache = new HashMap<>();
        for (UUID uid : userIds) {
            userClient.getUserById(uid).ifPresent(info -> userCache.put(uid, info));
        }

        return ordersPage.map(order -> orderMapper.toDTO(order, userCache.get(order.getUserId())));
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
