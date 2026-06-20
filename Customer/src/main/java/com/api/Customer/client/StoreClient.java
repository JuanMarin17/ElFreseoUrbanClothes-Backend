package com.api.Customer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class StoreClient {

    private final WebClient storeWebClient;

    public StoreClient(@Qualifier("storeWebClient") WebClient storeWebClient) {
        this.storeWebClient = storeWebClient;
    }

    /** Verifica si el usuario pertenece a la tienda (OWNER/ADMIN/STAFF). Falla cerrado ante error. */
    public boolean hasAccess(UUID storeId, UUID userId) {
        try {
            Map<String, Boolean> response = storeWebClient.get()
                    .uri("/stores/{storeId}/access/{userId}", storeId, userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Boolean>>() {})
                    .block();
            return response != null && Boolean.TRUE.equals(response.get("hasAccess"));
        } catch (Exception e) {
            log.warn("No se pudo validar acceso de userId={} a storeId={}: {}", userId, storeId, e.getMessage());
            return false;
        }
    }
}
