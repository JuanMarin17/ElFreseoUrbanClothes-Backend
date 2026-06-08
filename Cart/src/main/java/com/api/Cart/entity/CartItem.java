package com.api.Cart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "cart_item",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_cart_item_cart_product",
        columnNames = {"cart_id", "product_id"}
    )
)
public class CartItem {

    @Id
    @GeneratedValue
    @Column(name = "cart_item_id")
    private UUID cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /** ID del producto en el módulo Product */
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    /** Snapshot del nombre al momento de agregar */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /** Snapshot del SKU de la primera variante al momento de agregar */
    @Column(name = "product_sku")
    private String productSku;

    /** Snapshot de la imagen al momento de agregar */
    @Column(name = "product_image_url")
    private String productImageUrl;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** Precio unitario al momento de agregar al carrito */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "added_at", updatable = false)
    @Builder.Default
    private OffsetDateTime addedAt = OffsetDateTime.now();
}
