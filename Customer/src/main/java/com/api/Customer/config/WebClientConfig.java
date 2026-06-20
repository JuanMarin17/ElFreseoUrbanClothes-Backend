package com.api.Customer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("storeWebClient")
    public WebClient storeWebClient(WebClient.Builder builder, @Value("${store.service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
