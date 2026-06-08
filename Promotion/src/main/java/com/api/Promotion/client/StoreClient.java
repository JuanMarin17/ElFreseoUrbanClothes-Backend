package com.api.Promotion.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Component
@Slf4j
public class StoreClient {

    private final RestClient restClient;

    public StoreClient(@Value("${store.service.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public boolean existsStore(UUID storeId) {
        try {
            restClient.get()
                    .uri("/stores/existStore/{storeId}", storeId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("No se pudo verificar la tienda {}: {}", storeId, e.getMessage());
            return false;
        }
    }
}
