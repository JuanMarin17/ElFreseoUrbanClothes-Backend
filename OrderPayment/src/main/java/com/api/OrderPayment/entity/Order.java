package com.api.OrderPayment.entity;

import com.api.OrderPayment.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa una orden de compra generada a partir de un carrito.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Usuario que realizó la orden */
    @Column(nullable = false)
    private UUID userId;

    /** Tienda a la que pertenece la orden */
    @Column(nullable = false)
    private UUID storeId;

    /** Número de orden legible (ej: ORD-20240101-0001) */
    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /** Ítems de la orden (snapshot del carrito al momento de crear la orden) */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /** Subtotal antes de impuestos/descuentos */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /** Impuesto aplicado */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    /** Descuento aplicado */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    /** Total final (subtotal + tax - discount) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /** Dirección de envío */
    @Column
    private String shippingAddress;

    /** Notas adicionales del cliente */
    @Column(length = 500)
    private String notes;

    /** Pago asociado a la orden (puede ser null si aún no se pagó) */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
