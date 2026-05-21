package com.api.OrderPayment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Snapshot de un ítem del carrito al momento de crear la orden.
 * Se guarda independientemente del carrito para mantener el historial.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** ID del producto en el momento de la orden */
    @Column(nullable = false)
    private UUID productId;

    /** Nombre del producto (snapshot) */
    @Column(nullable = false)
    private String productName;

    /** Cantidad ordenada */
    @Column(nullable = false)
    private Integer quantity;

    /** Precio unitario al momento de la orden (snapshot) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** Subtotal de este ítem (quantity * unitPrice) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
