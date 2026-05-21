package com.api.Cart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Referencia mínima a la tabla "store".
 * El módulo Cart solo necesita verificar que una tienda exista,
 * no gestiona tiendas directamente.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "store")
public class StoreRef {

    @Id
    @Column(name = "store_id")
    private UUID storeId;
}
