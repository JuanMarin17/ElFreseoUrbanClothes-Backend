package com.api.product.client;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreClient {

    private final WebClient storeWebClient;

    public boolean existsStore(UUID storeId) {
        try {
            Boolean exists = storeWebClient.get()
                    .uri("/stores/existStore/{id}", storeId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            return false;
        }
    }

    public String userRole(UUID userId, UUID storeId){
        try{
            String role = storeWebClient.get()
                            .uri("/stores/{storeId}/isOwner/{userId}", storeId, userId)
                            .retrieve()
                            .bodyToMono(String.class)
                            .timeout(Duration.ofSeconds(5))
                            .block();
            // Store devuelve un enum (StoreRole), que al serializarse a JSON queda como
            // un string entre comillas (ej. "OWNER"). bodyToMono(String.class) entrega
            // ese texto crudo tal cual, comillas incluidas, por lo que sin este strip
            // "OWNER".equals(role) nunca es true.
            if (role != null) {
                role = role.replace("\"", "");
            }
            log.info("[StoreClient.userRole] storeId={} userId={} role={}", storeId, userId, role);
            return role;
        } catch(Exception e){
            log.error("[StoreClient.userRole] storeId={} userId={} fallo: {}", storeId, userId, e.getMessage());
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}