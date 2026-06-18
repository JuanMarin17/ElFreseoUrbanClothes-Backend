package com.api.Store.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class NotificationClient {

    private final WebClient notificationWebClient;

    public NotificationClient(@Qualifier("notificationWebClient") WebClient notificationWebClient) {
        this.notificationWebClient = notificationWebClient;
    }

    /** Fire-and-forget: notifica al owner via SSE que su tienda fue inhabilitada/habilitada */
    public void sendStoreStatusChanged(UUID ownerId, UUID storeId, String storeName, boolean isActive, String reason) {
        notificationWebClient.post()
                .uri("/internal/notifications/user/{userId}/store-status", ownerId)
                .bodyValue(Map.of(
                        "storeId", storeId,
                        "storeName", storeName != null ? storeName : "",
                        "isActive", isActive,
                        "reason", reason != null ? reason : ""))
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(2))
                .subscribe(
                        r -> {},
                        e -> log.warn("No se pudo enviar {} SSE a userId={}: {}",
                                isActive ? "STORE_ENABLED" : "STORE_DISABLED", ownerId, e.getMessage()));
    }
}
