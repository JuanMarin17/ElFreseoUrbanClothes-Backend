package com.api.reports.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient productWebClient(WebClient.Builder builder,
            @Value("${services.product.url:http://localhost:8084/api/v1}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean
    public WebClient orderWebClient(WebClient.Builder builder,
            @Value("${services.order.url:http://localhost:8087/api/v1}") String url) {
        return builder.baseUrl(url).build();
    }
}
