package com.api.Inventory.client;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final WebClient productWebClient;

    public void increaseStock(UUID variantId, Integer amount) {
        productWebClient.patch()
                .uri("/api/v1/variants/{variantId}/stock/increase", variantId)
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void decreaseStock(UUID variantId, Integer amount) {
        productWebClient.patch()
                .uri("/api/v1/variants/{variantId}/stock/decrease", variantId)
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public boolean variantExists(UUID variantId) {
        try {
            productWebClient.get()
                    .uri("/api/v1/variants/{variantId}", variantId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}