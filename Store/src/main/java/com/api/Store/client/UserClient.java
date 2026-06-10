package com.api.Store.client;

import com.api.Store.client.dto.UserInfoDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class UserClient {
    private final WebClient webClient;

    public UserClient(WebClient userWebClient){
        this.webClient = userWebClient;
    }

    public Boolean existUser(UUID id){
        return webClient.get()
                        .uri("/users/existUser/{id}", id)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block();
    }

    public Optional<UserInfoDTO> getUserById(UUID id) {
        try {
            UserInfoDTO info = webClient.get()
                    .uri("/users/getUserById/{id}", id)
                    .retrieve()
                    .bodyToMono(UserInfoDTO.class)
                    .block();
            return Optional.ofNullable(info);
        } catch (Exception e) {
            log.warn("No se pudo obtener info del usuario {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }
}
