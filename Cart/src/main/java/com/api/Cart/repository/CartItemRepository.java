package com.api.Cart.repository;

import com.api.Cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartItemIdAndCart_CartId(UUID cartItemId, UUID cartId);

    Optional<CartItem> findByCart_CartIdAndProductId(UUID cartId, UUID productId);

    void deleteAllByCart_CartId(UUID cartId);
}
