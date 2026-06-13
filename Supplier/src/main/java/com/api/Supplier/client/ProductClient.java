package com.api.Supplier.client;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.api.Supplier.dto.ProductSummaryDTO;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${product.service.url:http://localhost:8084/api/v1}") String productServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .build();
    }

    public Optional<ProductSummaryDTO> getProductById(UUID productId, UUID storeId) {
        try {
            ProductSummaryDTO product = restClient.get()
                    .uri("/products/{id}", productId)
                    .header("X-Store-Id", storeId.toString())
                    .retrieve()
                    .body(ProductSummaryDTO.class);
            return Optional.ofNullable(product);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean productExistsInStore(UUID productId, UUID storeId) {
        try {
            restClient.get()
                    .uri("/products/{id}", productId)
                    .header("X-Store-Id", storeId.toString())
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
