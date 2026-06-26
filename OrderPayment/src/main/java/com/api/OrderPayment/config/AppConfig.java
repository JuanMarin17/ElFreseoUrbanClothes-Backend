package com.api.OrderPayment.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * Configuración general de la aplicación.
 * - WebClient para comunicación con el módulo Cart (puerto 8086)
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Value("${cart.service.base-url}")
    private String cartBaseUrl;

    @Value("${users.service.url}")
    private String usersBaseUrl;

    // Las conexiones pooleadas inactivas pueden morir a nivel de red (Docker/NAT)
    // sin que Reactor Netty lo note: al reutilizarlas el cliente puede quedarse
    // esperando una respuesta que nunca llega, o peor, recibir la respuesta de
    // una petición anterior desincronizada en esa misma conexión reciclada.
    // maxIdleTime fuerza a descartarlas antes de que eso pase.
    private static final ConnectionProvider POOL = ConnectionProvider.builder("inter-service-pool")
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofMinutes(5))
            .evictInBackground(Duration.ofSeconds(30))
            .build();

    private static ReactorClientHttpConnector connector() {
        HttpClient httpClient = HttpClient.create(POOL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(5));
        return new ReactorClientHttpConnector(httpClient);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(cartBaseUrl)
                .clientConnector(connector())
                .build();
    }

    @Bean("usersWebClient")
    public WebClient usersWebClient() {
        return WebClient.builder()
                .baseUrl(usersBaseUrl)
                .clientConnector(connector())
                .build();
    }
}
