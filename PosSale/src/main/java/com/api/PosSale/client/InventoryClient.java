package com.api.PosSale.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClient {

    private final WebClient webClient;

    @Value("${inventory.service.base-url}")
    private String baseUrl;

    /**
     * Registra una salida de inventario para la variante vendida.
     * El servicio de Inventory usa el header X-Store-Id y X-User-Id para el contexto.
     */
    public void registerOutMovement(UUID storeId, UUID variantId, int quantity) {
        try {
            webClient.post()
                    .uri(baseUrl + "/inventory/movements")
                    .header("X-Store-Id", storeId.toString())
                    .bodyValue(Map.of(
                            "variantId", variantId.toString(),
                            "quantity", quantity,
                            "movementType", "OUT"
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Movimiento OUT registrado: variantId={}, qty={}", variantId, quantity);
        } catch (Exception e) {
            log.warn("No se pudo registrar movimiento de inventario para variantId={}: {}", variantId, e.getMessage());
        }
    }
}
