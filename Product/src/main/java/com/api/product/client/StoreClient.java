package com.api.product.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreClient {

    private final WebClient storeWebClient;

    public boolean existsStore(UUID storeId) {
        try {
            Boolean exists = storeWebClient.get()
                    .uri("/api/stores/existStore/{id}", storeId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            return false;
        }
    }
}