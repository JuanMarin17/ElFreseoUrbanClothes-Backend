package com.api.product.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {

    // Las conexiones pooleadas que quedan inactivas un rato pueden morir a nivel
    // de red (Docker/NAT cierra el socket sin que Reactor Netty lo note) sin que
    // el pool lo detecte al reutilizarlas: el cliente escribe la petición y se
    // queda esperando una respuesta que nunca llega hasta el timeout — de ahí
    // los cuelgues intermitentes de varios segundos al llamar a otros servicios.
    // maxIdleTime fuerza a descartar conexiones inactivas antes de que eso pase.
    private static final ConnectionProvider POOL = ConnectionProvider.builder("inter-service-pool")
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofMinutes(5))
            .evictInBackground(Duration.ofSeconds(30))
            .build();

    @Bean
    public WebClient storeWebClient(WebClient.Builder builder,
            @Value("${store.service.url}") String baseUrl) {
        HttpClient httpClient = HttpClient.create(POOL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(5));
        return builder.baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}