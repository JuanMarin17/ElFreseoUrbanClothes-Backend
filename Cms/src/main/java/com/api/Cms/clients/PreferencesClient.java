package com.api.Cms.clients;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PreferencesClient {

    private final WebClient preferencesWebClient;

    public List<Map<String, Object>> getUserBehaviors(String userId) {
        try {
            return preferencesWebClient.get()
                    .uri("/api/v1/preferences/behavior")
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .cast(Map.class)
                    .map(m -> (Map<String, Object>) m)
                    .collectList()
                    .block();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Map<String, Object>> getUserPreferences(String userId) {
        try {
            return preferencesWebClient.get()
                    .uri("/api/v1/preferences")
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .cast(Map.class)
                    .map(m -> (Map<String, Object>) m)
                    .collectList()
                    .block();
        } catch (Exception e) {
            return List.of();
        }
    }
}