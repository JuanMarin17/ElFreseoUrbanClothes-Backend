package com.user.api.user.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.user.api.user.dto.UserRegisterDTO;

@Component
public class UsersClient {
    private final WebClient userWebClient;

    public UsersClient(WebClient userClient){
        this.userWebClient = userClient;
    }

    public void createUser(UserRegisterDTO userRegisterDTO){
        userWebClient.post()
                    .uri("/users/createUser")
                    .bodyValue(userRegisterDTO)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
    }

    public String getUserName(UUID id){
        return userWebClient.get()
                        .uri("/users/myUserName/{id}", id)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
    }
}
