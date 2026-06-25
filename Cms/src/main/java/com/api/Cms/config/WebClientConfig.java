package com.api.Cms.config;

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

    // Las conexiones pooleadas inactivas pueden morir a nivel de red (Docker/NAT)
    // sin que Reactor Netty lo note: al reutilizarlas el cliente se queda
    // esperando una respuesta que nunca llega hasta el timeout. maxIdleTime
    // fuerza a descartarlas antes de que eso pase.
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
    public WebClient preferencesWebClient(WebClient.Builder builder,
            @Value("${preferences.service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).clientConnector(connector()).build();
    }

    @Bean
    public WebClient productWebClient(WebClient.Builder builder,
            @Value("${product.service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).clientConnector(connector()).build();
    }

    @Bean
    public WebClient storeWebClient(WebClient.Builder builder,
            @Value("${store.service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).clientConnector(connector()).build();
    }
}