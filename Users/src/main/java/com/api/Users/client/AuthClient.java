package com.api.Users.client;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.api.Users.client.dto.AuthUserInfoDTO;

@Component
public class AuthClient {
    private final WebClient authWebClient;

    public AuthClient(@Qualifier("authWebClient") WebClient authWebClient){
        this.authWebClient = authWebClient;
    }

    public String getEmail(UUID id){
        return authWebClient.get()
                            .uri("/auth/getEmailById/{id}", id)
                            .retrieve()
                            .bodyToMono(String.class)
                            .timeout(Duration.ofSeconds(5))
                            .block();
    }

    /** Trae el email de varios usuarios en una sola petición, en vez de una llamada por usuario. */
    public Map<UUID, AuthUserInfoDTO> getUserInfoBatch(List<UUID> ids){
        try {
            List<AuthUserInfoDTO> infos = authWebClient.post()
                    .uri("/auth/getUserInfoBatch")
                    .bodyValue(ids)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AuthUserInfoDTO>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (infos == null) return Collections.emptyMap();
            return infos.stream().collect(java.util.stream.Collectors.toMap(
                    AuthUserInfoDTO::getUserId, i -> i, (a, b) -> a));
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
