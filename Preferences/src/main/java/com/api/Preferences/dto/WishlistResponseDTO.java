package com.api.Preferences.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class WishlistResponseDTO {
    private UUID wishlistId;
    private UUID userId;
    private List<WishlistItemResponseDTO> items;
}