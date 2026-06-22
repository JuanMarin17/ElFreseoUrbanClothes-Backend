package com.api.gateway.filter;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    static final String HEADER = "X-Request-Id";

    @Override
    public int getOrder() {
        return -100; // Corre antes de RateLimitFilter (-50) y JwtValidationFilter
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String id = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        final String requestId = id;

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(HEADER, requestId)
                .build();

        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().add(HEADER, requestId);
            return Mono.empty();
        });

        return chain.filter(exchange.mutate().request(mutated).build());
    }
}
