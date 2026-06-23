package com.api.product.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.product.dto.StockAlertDTO;
import com.api.product.entity.ProductVariant;
import com.api.product.exception.BadRequestException;
import com.api.product.exception.ResourceNotFoundException;
import com.api.product.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VariantInventoryService {

    private final ProductVariantRepository variantRepository;
    private final StockAlertService stockAlertService;

    @Transactional
    public ProductVariant increaseStock(UUID variantId, Integer amount) {
        if (amount == null || amount <= 0)
            throw new BadRequestException("La cantidad a aumentar debe ser mayor a 0");

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada con id: " + variantId));

        variant.setStock(variant.getStock() + amount);
        ProductVariant saved = variantRepository.save(variant);
        checkAndSendAlert(saved);
        return saved;
    }

    @Transactional
    public ProductVariant decreaseStock(UUID variantId, Integer amount) {
        if (amount == null || amount <= 0)
            throw new BadRequestException("La cantidad a disminuir debe ser mayor a 0");

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada con id: " + variantId));

        if (variant.getStock() - amount < 0)
            throw new BadRequestException("Stock insuficiente, no se puede dejar negativo");

        variant.setStock(variant.getStock() - amount);
        ProductVariant saved = variantRepository.save(variant);
        checkAndSendAlert(saved);
        return saved;
    }

    @Transactional
    public ProductVariant setStock(UUID variantId, Integer quantity) {
        if (quantity == null || quantity < 0)
            throw new BadRequestException("El stock no puede ser negativo");

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada con id: " + variantId));

        variant.setStock(quantity);
        ProductVariant saved = variantRepository.save(variant);
        checkAndSendAlert(saved);
        return saved;
    }

    @Transactional
    public ProductVariant updateMinStock(UUID variantId, Integer minStock) {
        if (minStock == null || minStock < 0)
            throw new BadRequestException("El minStock no puede ser negativo");

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada con id: " + variantId));

        variant.setMinStock(minStock);
        ProductVariant saved = variantRepository.save(variant);
        checkAndSendAlert(saved);
        return saved;
    }

    private String buildVariantLabel(ProductVariant variant) {
        StringBuilder sb = new StringBuilder(" (SKU: ").append(variant.getSku());
        if (variant.getSize() != null && !variant.getSize().isBlank())
            sb.append(" | Talla: ").append(variant.getSize());
        if (variant.getColor() != null && !variant.getColor().isBlank())
            sb.append(" | Color: ").append(variant.getColor());
        sb.append(")");
        return sb.toString();
    }

    private void checkAndSendAlert(ProductVariant variant) {
        if (variant.getStock() <= variant.getMinStock()) {
            String variantLabel = buildVariantLabel(variant);
            stockAlertService.sendAlert(StockAlertDTO.builder()
                    .storeId(variant.getProduct().getStoreId())
                    .productId(variant.getProduct().getProductId())
                    .productName(variant.getProduct().getName())
                    .variantId(variant.getVariantId())
                    .sku(variant.getSku())
                    .size(variant.getSize())
                    .color(variant.getColor())
                    .stock(variant.getStock())
                    .minStock(variant.getMinStock())
                    .message("⚠️ Stock bajo: " + variant.getProduct().getName() + variantLabel)
                    .timestamp(OffsetDateTime.now())
                    .build());
        }
    }
}