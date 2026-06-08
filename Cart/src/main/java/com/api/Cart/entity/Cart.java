package com.api.Cart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Carrito de compras de un usuario en una tienda.
 * Cada usuario tiene máximo un carrito activo por tienda.
 * Se destruye al convertirse en Order.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "cart",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_cart_store_user",
        columnNames = {"store_id", "user_id"}
    )
)
public class Cart {

    @Id
    @GeneratedValue
    @Column(name = "cart_id")
    private UUID cartId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    /** ID del usuario comprador (puede ser UUID de tu módulo de Auth) */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
