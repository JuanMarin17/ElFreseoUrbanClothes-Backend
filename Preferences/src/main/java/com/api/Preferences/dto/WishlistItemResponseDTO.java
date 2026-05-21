package com.api.Preferences.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class WishlistItemResponseDTO {
    private UUID wishlistItemId;
    private UUID variantId;
}