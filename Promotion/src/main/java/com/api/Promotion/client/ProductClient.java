package com.api.Promotion.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Component
@Slf4j
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${product.service.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public boolean existsProduct(UUID productId, UUID storeId) {
        try {
            restClient.get()
                    .uri("/products/{id}", productId)
                    .header("X-Store-Id", storeId.toString())
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("No se pudo verificar el producto {}: {}", productId, e.getMessage());
            return false;
        }
    }
}
