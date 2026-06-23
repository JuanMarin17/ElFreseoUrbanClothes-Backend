package com.api.Store.client;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.api.Store.client.dto.AuthUserInfoDTO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthClient {

    private final WebClient webClient;

    public AuthClient(@Qualifier("authWebClient") WebClient authWebClient) {
        this.webClient = authWebClient;
    }

    public Optional<AuthUserInfoDTO> getUserInfo(UUID userId) {
        try {
            AuthUserInfoDTO info = webClient.get()
                    .uri("/auth/getUserInfo/{id}", userId)
                    .retrieve()
                    .bodyToMono(AuthUserInfoDTO.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return Optional.ofNullable(info);
        } catch (Exception e) {
            log.warn("No se pudo obtener info de Auth para usuario {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    /** Trae info de Auth para varios usuarios en una sola petición, en vez de una llamada por usuario. */
    public Map<UUID, AuthUserInfoDTO> getUserInfoBatch(List<UUID> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        try {
            List<AuthUserInfoDTO> infos = webClient.post()
                    .uri("/auth/getUserInfoBatch")
                    .bodyValue(userIds)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AuthUserInfoDTO>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (infos == null) return Collections.emptyMap();
            return infos.stream().collect(java.util.stream.Collectors.toMap(
                    AuthUserInfoDTO::getUserId, i -> i, (a, b) -> a));
        } catch (Exception e) {
            log.warn("No se pudo obtener info de Auth en batch: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
