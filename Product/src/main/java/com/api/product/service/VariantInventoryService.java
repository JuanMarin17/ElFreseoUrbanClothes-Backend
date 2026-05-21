package com.api.product.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.product.dto.StockAlertDTO;
import com.api.product.entity.ProductVariant;
import com.api.product.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VariantInventoryService {

    private final ProductVariantRepository variantRepository;
    private final StockAlertService stockAlertService;

    /**
     * Incrementa el stock de una variante.
     */
    @Transactional
    public ProductVariant increaseStock(UUID variantId, Integer amount) {

        if (amount == null || amount <= 0) {
            throw new RuntimeException("La cantidad a aumentar debe ser mayor a 0");
        }

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada"));

        variant.setStock(variant.getStock() + amount);

        ProductVariant saved = variantRepository.save(variant);

        checkAndSendAlert(saved);

        return saved;
    }

    /**
     * Disminuye el stock de una variante.
     */
    @Transactional
    public ProductVariant decreaseStock(UUID variantId, Integer amount) {

        if (amount == null || amount <= 0) {
            throw new RuntimeException("La cantidad a disminuir debe ser mayor a 0");
        }

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada"));

        if (variant.getStock() - amount < 0) {
            throw new RuntimeException("Stock insuficiente, no se puede dejar negativo");
        }

        variant.setStock(variant.getStock() - amount);

        ProductVariant saved = variantRepository.save(variant);

        checkAndSendAlert(saved);

        return saved;
    }

    /**
     * Actualiza el stock mínimo de una variante.
     */
    @Transactional
    public ProductVariant updateMinStock(UUID variantId, Integer minStock) {

        if (minStock == null || minStock < 0) {
            throw new RuntimeException("minStock inválido");
        }

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada"));

        variant.setMinStock(minStock);

        ProductVariant saved = variantRepository.save(variant);

        checkAndSendAlert(saved);

        return saved;
    }

    /**
     * Verifica si stock <= minStock y dispara alerta SSE.
     */
    private void checkAndSendAlert(ProductVariant variant) {

        if (variant.getStock() <= variant.getMinStock()) {

            stockAlertService.sendAlert(
                    StockAlertDTO.builder()
                            .productId(variant.getProduct().getProductId())
                            .productName(variant.getProduct().getName())
                            .variantId(variant.getVariantId())
                            .sku(variant.getSku())
                            .stock(variant.getStock())
                            .minStock(variant.getMinStock())
                            .message("⚠️ Stock bajo: " + variant.getProduct().getName() +
                                    " (SKU: " + variant.getSku() + ")")
                            .timestamp(OffsetDateTime.now())
                            .build()
            );
        }
    }
}