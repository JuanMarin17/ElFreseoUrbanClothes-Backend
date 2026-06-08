package com.api.product.controller;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.product.dto.ApiResponseDTO;
import com.api.product.dto.MinStockUpdateDTO;
import com.api.product.dto.StockUpdateDTO;
import com.api.product.entity.ProductVariant;
import com.api.product.service.VariantInventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
public class VariantInventoryController {

    private final VariantInventoryService inventoryService;

    /**
     * Aumentar stock.
     */
    @PatchMapping("/{variantId}/stock/increase")
    public ResponseEntity<ApiResponseDTO<ProductVariant>> increaseStock(
            @PathVariable UUID variantId,
            @RequestBody StockUpdateDTO dto) {

        ProductVariant variant = inventoryService.increaseStock(variantId, dto.getAmount());

        return ResponseEntity.ok(
                ApiResponseDTO.<ProductVariant>builder()
                        .message("Stock incrementado correctamente")
                        .status(HttpStatus.OK.value())
                        .data(variant)
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    /**
     * Disminuir stock.
     */
    @PatchMapping("/{variantId}/stock/decrease")
    public ResponseEntity<ApiResponseDTO<ProductVariant>> decreaseStock(
            @PathVariable UUID variantId,
            @RequestBody StockUpdateDTO dto) {

        ProductVariant variant = inventoryService.decreaseStock(variantId, dto.getAmount());

        return ResponseEntity.ok(
                ApiResponseDTO.<ProductVariant>builder()
                        .message("Stock reducido correctamente")
                        .status(HttpStatus.OK.value())
                        .data(variant)
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    /**
     * Actualizar minStock.
     */
    @PatchMapping("/{variantId}/min-stock")
    public ResponseEntity<ApiResponseDTO<ProductVariant>> updateMinStock(
            @PathVariable UUID variantId,
            @RequestBody MinStockUpdateDTO dto) {

        ProductVariant variant = inventoryService.updateMinStock(variantId, dto.getMinStock());

        return ResponseEntity.ok(
                ApiResponseDTO.<ProductVariant>builder()
                        .message("Stock mínimo actualizado correctamente")
                        .status(HttpStatus.OK.value())
                        .data(variant)
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }
}