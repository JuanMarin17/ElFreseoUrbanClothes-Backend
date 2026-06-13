package com.api.Users.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
                            .block();
    }
}
