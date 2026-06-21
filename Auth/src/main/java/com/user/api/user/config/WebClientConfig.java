package com.user.api.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    @Bean
    public WebClient userWebClient(WebClient.Builder builder, @Value("${users.service.url}") String baseUrl){
        return builder
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("notificationWebClient")
    public WebClient notificationWebClient(WebClient.Builder builder,
                                           @Value("${notifications.service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean("geoWebClient")
    public WebClient geoWebClient(WebClient.Builder builder, @Value("${geo.service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
