package com.api.OrderPayment.client;

import com.api.OrderPayment.client.dto.UserInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class UserClient {

    private final WebClient webClient;

    public UserClient(@Qualifier("usersWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Optional<UserInfoDTO> getUserById(UUID userId) {
        try {
            UserInfoDTO info = webClient.get()
                    .uri("/users/getUserById/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserInfoDTO.class)
                    .block();
            return Optional.ofNullable(info);
        } catch (Exception e) {
            log.warn("No se pudo obtener info del usuario {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }
}
