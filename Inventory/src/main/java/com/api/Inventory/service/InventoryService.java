package com.api.Inventory.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.Inventory.client.NotificationClient;
import com.api.Inventory.client.ProductClient;
import com.api.Inventory.dto.InventoryBalanceResponseDTO;
import com.api.Inventory.dto.MovementRequestDTO;
import com.api.Inventory.dto.MovementResponseDTO;
import com.api.Inventory.entity.InventoryBalance;
import com.api.Inventory.entity.InventoryMovement;
import com.api.Inventory.enums.MovementType;
import com.api.Inventory.exception.BadRequestException;
import com.api.Inventory.exception.UnauthorizedException;
import com.api.Inventory.repository.InventoryBalanceRepository;
import com.api.Inventory.repository.InventoryMovementRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryBalanceRepository balanceRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductClient productClient;
    private final NotificationClient notificationClient;

    // ── Registrar movimiento ──────────────────────────────────────────────────
    @Transactional
    public MovementResponseDTO registerMovement(MovementRequestDTO dto) {
        validateAdmin();
        UUID storeId = getStoreIdFromHeader();

        if (dto.getQuantity() == null || dto.getQuantity() <= 0)
            throw new BadRequestException("La cantidad debe ser mayor a 0");

        if (!productClient.variantExists(dto.getVariantId()))
            throw new BadRequestException("La variante no existe en el módulo de productos");

        // Obtener o crear balance
        InventoryBalance balance = balanceRepository
                .findByVariantIdAndLocationIdAndStoreId(dto.getVariantId(), dto.getLocationId(), storeId)
                .orElseGet(() -> {
                    InventoryBalance newBalance = new InventoryBalance();
                    newBalance.setVariantId(dto.getVariantId());
                    newBalance.setLocationId(dto.getLocationId());
                    newBalance.setStoreId(storeId);
                    newBalance.setQuantity(0);
                    return newBalance;
                });

        // Aplicar movimiento al balance y actualizar stock en Product
        if (dto.getMovementType() == MovementType.IN) {
            balance.setQuantity(balance.getQuantity() + dto.getQuantity());
            productClient.increaseStock(dto.getVariantId(), dto.getQuantity());

        } else if (dto.getMovementType() == MovementType.OUT) {
            if (balance.getQuantity() - dto.getQuantity() < 0)
                throw new BadRequestException("Stock insuficiente en esta ubicación");
            balance.setQuantity(balance.getQuantity() - dto.getQuantity());
            productClient.decreaseStock(dto.getVariantId(), dto.getQuantity());

        } else if (dto.getMovementType() == MovementType.ADJUSTMENT) {
            int difference = dto.getQuantity() - balance.getQuantity();
            if (difference > 0) {
                productClient.increaseStock(dto.getVariantId(), difference);
            } else if (difference < 0) {
                productClient.decreaseStock(dto.getVariantId(), Math.abs(difference));
            }
            balance.setQuantity(dto.getQuantity());
        }

        balanceRepository.save(balance);

        // Alerta de stock al owner de la tienda cuando el balance cae a nivel crítico
        if (dto.getMovementType() != MovementType.IN) {
            int qty = balance.getQuantity();
            if (qty == 0 || qty <= 5) {
                String type    = qty == 0 ? "OUT_OF_STOCK"   : "LOW_STOCK_ALERT";
                String title   = qty == 0 ? "Producto agotado" : "Stock crítico";
                String message = qty == 0
                        ? "La variante " + dto.getVariantId() + " se ha agotado"
                        : "La variante " + dto.getVariantId() + " tiene solo " + qty + " unidades";
                try {
                    notificationClient.notifyStore(storeId, type, title, message,
                            Map.of("variantId", dto.getVariantId(), "quantity", qty));
                } catch (Exception e) {
                    log.warn("No se pudo enviar alerta de stock a storeId={}: {}", storeId, e.getMessage());
                }
            }
        }

        // Registrar movimiento
        InventoryMovement movement = new InventoryMovement();
        movement.setVariantId(dto.getVariantId());
        movement.setStoreId(storeId);
        movement.setQuantity(dto.getQuantity());
        movement.setMovementType(dto.getMovementType());

        return toMovementResponse(movementRepository.save(movement));
    }

    // ── Obtener balance de la tienda ──────────────────────────────────────────
    public List<InventoryBalanceResponseDTO> getBalanceByStore() {
        UUID storeId = getStoreIdFromHeader();
        return balanceRepository.findByStoreId(storeId)
                .stream().map(this::toBalanceResponse).toList();
    }

    // ── Obtener movimientos de la tienda ──────────────────────────────────────
    public List<MovementResponseDTO> getMovementsByStore() {
        UUID storeId = getStoreIdFromHeader();
        return movementRepository.findByStoreId(storeId)
                .stream().map(this::toMovementResponse).toList();
    }

    // ── Obtener movimientos por variante ──────────────────────────────────────
    public List<MovementResponseDTO> getMovementsByVariant(UUID variantId) {
        UUID storeId = getStoreIdFromHeader();
        return movementRepository.findByVariantIdAndStoreId(variantId, storeId)
                .stream().map(this::toMovementResponse).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private UUID getStoreIdFromHeader() {
        String storeIdHeader = RequestContext.getHeader("X-Store-Id");
        if (storeIdHeader == null || storeIdHeader.isBlank())
            throw new BadRequestException("No se encontró el X-Store-Id en el header");
        try {
            return UUID.fromString(storeIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del storeId");
        }
    }

    private void validateAdmin() {
        String role = RequestContext.getHeader("X-User-Role");
        if (!"ADMIN".equals(role))
            throw new UnauthorizedException("Solo el ADMIN puede realizar esta acción");
    }

    // ── Mappers ───────────────────────────────────────────────────────────────
    private MovementResponseDTO toMovementResponse(InventoryMovement m) {
        MovementResponseDTO dto = new MovementResponseDTO();
        dto.setMovementId(m.getMovementId());
        dto.setVariantId(m.getVariantId());
        dto.setStoreId(m.getStoreId());
        dto.setQuantity(m.getQuantity());
        dto.setMovementType(m.getMovementType());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }

    private InventoryBalanceResponseDTO toBalanceResponse(InventoryBalance b) {
        InventoryBalanceResponseDTO dto = new InventoryBalanceResponseDTO();
        dto.setBalanceId(b.getBalanceId());
        dto.setVariantId(b.getVariantId());
        dto.setLocationId(b.getLocationId());
        dto.setStoreId(b.getStoreId());
        dto.setQuantity(b.getQuantity());
        return dto;
    }
}