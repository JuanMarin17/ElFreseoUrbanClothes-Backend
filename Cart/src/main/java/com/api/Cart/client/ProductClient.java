package com.api.Cart.client;

import com.api.Cart.client.dto.ApiWrapper;
import com.api.Cart.client.dto.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${product.service.url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Busca un producto activo por ID.
     * Devuelve Optional.empty() si no existe, está inactivo o el servicio no responde.
     */
    public Optional<ProductResponse> findActiveProduct(UUID productId, UUID storeId) {
        try {
            ApiWrapper<ProductResponse> response = restClient.get()
                    .uri("/products/{id}", productId)
                    .header("X-Store-Id", storeId.toString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || response.getData() == null) return Optional.empty();

            ProductResponse product = response.getData();
            if (!product.isActive()) return Optional.empty();

            return Optional.of(product);

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            log.warn("No se pudo obtener el producto {}: {}", productId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Obtiene el precio actual de un producto (para detectar cambios de precio).
     * Si el servicio no responde, devuelve null para evitar falsos positivos.
     */
    public ProductResponse findProductForPriceCheck(UUID productId, UUID storeId) {
        try {
            ApiWrapper<ProductResponse> response = restClient.get()
                    .uri("/products/{id}", productId)
                    .header("X-Store-Id", storeId.toString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            return response != null ? response.getData() : null;

        } catch (Exception e) {
            log.warn("No se pudo verificar precio del producto {}: {}", productId, e.getMessage());
            return null;
        }
    }

    /**
     * Verifica el precio actual de varios productos en una sola petición (batch),
     * para evitar una llamada HTTP por cada ítem del carrito.
     */
    public Map<UUID, ProductResponse> findProductsForPriceCheck(List<UUID> productIds, UUID storeId) {
        if (productIds.isEmpty()) return Map.of();
        try {
            ApiWrapper<List<ProductResponse>> response = restClient.post()
                    .uri("/products/batch")
                    .header("X-Store-Id", storeId.toString())
                    .body(productIds)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || response.getData() == null) return Map.of();

            return response.getData().stream()
                    .collect(Collectors.toMap(ProductResponse::getProductId, p -> p, (a, b) -> a));

        } catch (Exception e) {
            log.warn("No se pudo verificar precios en batch: {}", e.getMessage());
            return Map.of();
        }
    }
}
