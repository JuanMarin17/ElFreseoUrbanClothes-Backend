package com.api.Store.client;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
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
                    .block();
            return Optional.ofNullable(info);
        } catch (Exception e) {
            log.warn("No se pudo obtener info de Auth para usuario {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }
}
