package com.api.Support.client;

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

    public void notifyUser(UUID userId, String type, String title, String message) {
        notificationWebClient.post()
                .uri("/internal/notifications/user/{userId}", userId)
                .bodyValue(Map.of("type", type, "title", title, "message", message))
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(2))
                .subscribe(
                        r -> {},
                        e -> log.warn("No se pudo enviar notificación SSE a userId={}: {}", userId, e.getMessage()));
    }
}
