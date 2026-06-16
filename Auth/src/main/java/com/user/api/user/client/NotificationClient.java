package com.user.api.user.client;

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

    /** Fire-and-forget: notifica via SSE a las sesiones activas del usuario */
    public void sendSessionAlert(UUID userId, String ip, String userAgent) {
        notificationWebClient.post()
                .uri("/internal/notifications/user/{userId}/session-alert", userId)
                .bodyValue(Map.of(
                        "ip", ip != null ? ip : "",
                        "userAgent", userAgent != null ? userAgent : ""))
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(2))
                .subscribe(
                        r -> {},
                        e -> log.warn("No se pudo enviar SESSION_ALERT SSE a userId={}: {}", userId, e.getMessage()));
    }
}
