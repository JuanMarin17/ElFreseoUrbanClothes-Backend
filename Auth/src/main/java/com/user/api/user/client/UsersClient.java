package com.user.api.user.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.user.api.user.dto.UserRegisterDTO;
import com.user.api.user.exception.UserAlreadyExistsException;

import reactor.core.publisher.Mono;

@Component
public class UsersClient {
    private final WebClient userWebClient;

    public UsersClient(WebClient userClient){
        this.userWebClient = userClient;
    }

    public void createUser(UserRegisterDTO userRegisterDTO){
        try {
            userWebClient.post()
                        .uri("/users/createUser")
                        .bodyValue(userRegisterDTO)
                        .retrieve()
                        .onStatus(
                            status -> status.is4xxClientError(),
                            response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                    new UserAlreadyExistsException("Nombre de usuario no disponible: " + userRegisterDTO.getUserName())))
                        )
                        .toBodilessEntity()
                        .block();
        } catch (WebClientResponseException e) {
            throw new UserAlreadyExistsException("Nombre de usuario no disponible: " + userRegisterDTO.getUserName());
        }
    }

    public String getUserName(UUID id){
        return userWebClient.get()
                        .uri("/users/myUserName/{id}", id)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
    }
}
