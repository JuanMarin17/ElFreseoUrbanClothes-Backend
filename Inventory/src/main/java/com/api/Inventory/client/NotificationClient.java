package com.api.Inventory.client;

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

    public void notifyStore(UUID storeId, String type, String title, String message,
                            Map<String, Object> data) {
        notificationWebClient.post()
                .uri("/internal/notifications/store/{storeId}", storeId)
                .bodyValue(Map.of("type", type, "title", title, "message", message, "data", data))
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(2))
                .subscribe(
                        r -> {},
                        e -> log.warn("No se pudo enviar notificación de stock a storeId={}: {}", storeId, e.getMessage()));
    }
}
