package com.api.PosSale.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class AppConfig {

    // Las conexiones pooleadas inactivas pueden morir a nivel de red (Docker/NAT)
    // sin que Reactor Netty lo note: al reutilizarlas el cliente puede quedarse
    // esperando una respuesta que nunca llega, o recibir la respuesta de una
    // petición anterior desincronizada en esa conexión reciclada. maxIdleTime
    // fuerza a descartarlas antes de que eso pase.
    private static final ConnectionProvider POOL = ConnectionProvider.builder("inter-service-pool")
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofMinutes(5))
            .evictInBackground(Duration.ofSeconds(30))
            .build();

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create(POOL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(5));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
