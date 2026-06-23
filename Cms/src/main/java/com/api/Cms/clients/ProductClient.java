package com.api.Cms.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final WebClient productWebClient;

    public List<Map<String, Object>> getActiveProducts(String storeId) {
        try {
            return productWebClient.get()
                    .uri("/api/v1/products/all/active")
                    .header("X-Store-Id", storeId)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .cast(Map.class)
                    .map(m -> (Map<String, Object>) m)
                    .collectList()
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            return List.of();
        }
    }
}