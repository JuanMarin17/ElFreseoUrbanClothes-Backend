package com.api.Store.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
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
}
