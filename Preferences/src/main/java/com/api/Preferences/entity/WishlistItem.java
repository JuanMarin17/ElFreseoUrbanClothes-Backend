package com.api.Preferences.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wishlist_item")
public class WishlistItem {

    @Id
    @GeneratedValue
    @Column(name = "wishlist_item_id")
    private UUID wishlistItemId;

    @Column(name = "wishlist_id")
    private UUID wishlistId;

    @Column(name = "variant_id")
    private UUID variantId;
}