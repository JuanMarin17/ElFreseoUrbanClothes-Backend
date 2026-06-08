package com.api.Cart.client;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.api.Cart.client.dto.ProductPromotionDTO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PromotionClient {

    private final RestClient restClient;

    public PromotionClient(@Value("${promotion.service.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Obtiene las promociones activas para los productos dados en una tienda.
     * Si el servicio no responde devuelve lista vacía para no bloquear el carrito.
     */
    public List<ProductPromotionDTO> getPromotionsForProducts(List<UUID> productIds, UUID storeId) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        try {
            String uri = UriComponentsBuilder
                    .fromPath("/promotions/internal/by-products")
                    .queryParam("productIds", productIds.toArray())
                    .build()
                    .toUriString();

            List<ProductPromotionDTO> result = restClient.get()
                    .uri(uri)
                    .header("X-Store-Id", storeId.toString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            return result != null ? result : List.of();
        } catch (Exception e) {
            log.warn("No se pudieron obtener promociones de productos: {}", e.getMessage());
            return List.of();
        }
    }
}
