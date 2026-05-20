package com.api.Users.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AuthClient {
    private final WebClient authWebClient;

    public AuthClient(WebClient authClient){
        this.authWebClient = authClient;
    }

    public String getEmail(UUID id){
        return authWebClient.get()
                            .uri("/auth/getEmailById/{id}", id)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
    }
}
