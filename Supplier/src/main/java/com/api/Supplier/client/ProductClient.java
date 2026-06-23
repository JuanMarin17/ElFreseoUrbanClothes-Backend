package com.api.Supplier.client;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.api.Supplier.dto.ApiResponseWrapper;
import com.api.Supplier.dto.ProductSummaryDTO;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${product.service.url:http://localhost:8084/api/v1}") String productServiceUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);
        this.restClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .requestFactory(requestFactory)
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

    /** Trae varios productos en una sola petición, en vez de una llamada por producto vinculado al proveedor. */
    public List<ProductSummaryDTO> getProductsByIds(List<UUID> productIds, UUID storeId) {
        if (productIds.isEmpty()) return Collections.emptyList();
        try {
            ApiResponseWrapper<List<ProductSummaryDTO>> response = restClient.post()
                    .uri("/products/batch")
                    .header("X-Store-Id", storeId.toString())
                    .body(productIds)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return response != null && response.getData() != null ? response.getData() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
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
