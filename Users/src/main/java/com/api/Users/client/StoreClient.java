package com.api.Users.client;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StoreClient {

    private final WebClient storeWebClient;

    public StoreClient(@Qualifier("storeWebClient") WebClient storeWebClient) {
        this.storeWebClient = storeWebClient;
    }

    public List<StoreInfo> getAllStores() {
        try {
            List<StoreInfo> stores = storeWebClient.get()
                    .uri("/stores")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<StoreInfo>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return stores != null ? stores : Collections.emptyList();
        } catch (Exception e) {
            log.warn("No se pudo obtener la lista de tiendas: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Data
    public static class StoreInfo {
        private UUID storeId;
        private UUID ownerId;
        private String name;
        private String slug;
        private Boolean isActive;
    }
}
