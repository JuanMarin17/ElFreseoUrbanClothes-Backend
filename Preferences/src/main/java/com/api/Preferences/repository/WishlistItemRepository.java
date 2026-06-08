package com.api.Preferences.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.Preferences.entity.WishlistItem;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {
    List<WishlistItem> findByWishlistId(UUID wishlistId);

    boolean existsByWishlistIdAndVariantId(UUID wishlistId, UUID variantId);
}