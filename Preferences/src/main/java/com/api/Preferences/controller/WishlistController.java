package com.api.Preferences.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Preferences.dto.ApiResponseDTO;
import com.api.Preferences.dto.WishlistItemRequestDTO;
import com.api.Preferences.dto.WishlistResponseDTO;
import com.api.Preferences.service.WishlistService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/getMyWishlist")
    public ResponseEntity<WishlistResponseDTO> getMyWishlist() {
        return ResponseEntity.ok(wishlistService.getMyWishlist());
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistResponseDTO> addItem(@RequestBody WishlistItemRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.addItem(dto));
    }

    @DeleteMapping("/items/{wishlistItemId}")
    public ResponseEntity<ApiResponseDTO> removeItem(@PathVariable UUID wishlistItemId) {
        return ResponseEntity.ok(wishlistService.removeItem(wishlistItemId));
    }
}