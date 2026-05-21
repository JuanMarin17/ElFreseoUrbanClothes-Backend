package com.api.Cart.repository;

import com.api.Cart.entity.StoreRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio de solo lectura sobre la tabla "store".
 * El módulo Cart no gestiona tiendas, solo verifica que existan.
 */
public interface StoreRepository extends JpaRepository<StoreRef, UUID> {
}
