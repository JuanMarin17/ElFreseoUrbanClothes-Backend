package com.api.Cms.clients;

import java.time.Duration;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreClient {

    private final WebClient storeWebClient;

    /** GET /stores/getByStoreId/{storeId} — solo name y description, para darle contexto al prompt de IA. */
    @SuppressWarnings("unchecked")
    public Map<String, String> getStoreInfo(String storeId) {
        try {
            Map<String, Object> body = storeWebClient.get()
                    .uri("/stores/getByStoreId/{storeId}", storeId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            if (body == null) return Map.of();
            return Map.of(
                    "name", String.valueOf(body.getOrDefault("name", "")),
                    "description", String.valueOf(body.getOrDefault("description", "")));
        } catch (Exception e) {
            return Map.of();
        }
    }
}
