package com.api.Returns.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderClient {

    private final WebClient orderWebClient;

    public boolean existsOrder(UUID storeId, UUID orderId) {
        try {
            orderWebClient.get()
                    .uri("/api/stores/{storeId}/orders/{orderId}", storeId, orderId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}