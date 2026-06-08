package com.api.OrderPayment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración general de la aplicación.
 * - WebClient para comunicación con el módulo Cart (puerto 8086)
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Value("${cart.service.base-url}")
    private String cartBaseUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(cartBaseUrl)
                .build();
    }
}
