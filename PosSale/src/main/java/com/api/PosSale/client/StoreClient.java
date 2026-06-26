package com.api.PosSale.client;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoreClient {

    private final WebClient webClient;

    @Value("${store.service.base-url}")
    private String baseUrl;

    /** GET /stores/{storeId}/isOwner/{userId} — rol del usuario en esa tienda específica (OWNER/ADMIN/EMPLOYEE), o null si no pertenece. */
    public String userRole(UUID userId, UUID storeId) {
        try {
            String role = webClient.get()
                    .uri(baseUrl + "/stores/{storeId}/isOwner/{userId}", storeId, userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            // Store devuelve un enum serializado a JSON ("OWNER", con comillas) —
            // bodyToMono(String.class) entrega el texto crudo, comillas incluidas.
            return role != null ? role.replace("\"", "") : null;
        } catch (Exception e) {
            log.warn("No se pudo obtener el rol del usuario {} en la tienda {}: {}", userId, storeId, e.getMessage());
            return null;
        }
    }
}
