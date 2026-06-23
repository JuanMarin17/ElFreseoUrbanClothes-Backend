package com.api.PosSale.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
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
     * Registra varios movimientos de inventario en una sola petición,
     * en vez de una llamada HTTP por cada ítem de la venta.
     */
    public void registerMovementsBatch(UUID storeId, List<Map<String, Object>> movements) {
        if (movements.isEmpty()) return;
        try {
            webClient.post()
                    .uri(baseUrl + "/inventory/movements/batch")
                    .header("X-Store-Id", storeId.toString())
                    .bodyValue(movements)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.info("Batch de {} movimientos registrado para storeId={}", movements.size(), storeId);
        } catch (Exception e) {
            log.warn("No se pudo registrar el batch de movimientos para storeId={}: {}", storeId, e.getMessage());
        }
    }

    public static Map<String, Object> movement(UUID variantId, int quantity, String movementType) {
        return Map.of(
                "variantId", variantId.toString(),
                "quantity", quantity,
                "movementType", movementType
        );
    }
}
