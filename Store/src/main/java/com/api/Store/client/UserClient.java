package com.api.Store.client;

import com.api.Store.client.dto.UserInfoDTO;
import lombok.extern.slf4j.Slf4j;

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

@Component
@Slf4j
public class UserClient {
    private final WebClient webClient;

    public UserClient(@Qualifier("userWebClient") WebClient userWebClient){
        this.webClient = userWebClient;
    }

    public Boolean existUser(UUID id){
        return webClient.get()
                        .uri("/users/existUser/{id}", id)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .timeout(Duration.ofSeconds(5))
                        .block();
    }

    public Optional<UserInfoDTO> getUserById(UUID id) {
        try {
            UserInfoDTO info = webClient.get()
                    .uri("/users/getUserById/{id}", id)
                    .retrieve()
                    .bodyToMono(UserInfoDTO.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return Optional.ofNullable(info);
        } catch (Exception e) {
            log.warn("No se pudo obtener info del usuario {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /** Trae varios usuarios en una sola petición, en vez de una llamada por usuario. */
    public Map<UUID, UserInfoDTO> getUsersByIds(List<UUID> ids) {
        if (ids.isEmpty()) return Collections.emptyMap();
        try {
            List<UserInfoDTO> infos = webClient.post()
                    .uri("/users/batch")
                    .bodyValue(ids)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<UserInfoDTO>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (infos == null) return Collections.emptyMap();
            return infos.stream().collect(java.util.stream.Collectors.toMap(
                    UserInfoDTO::getUserId, i -> i, (a, b) -> a));
        } catch (Exception e) {
            log.warn("No se pudo obtener info de usuarios en batch: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
