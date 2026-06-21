package com.api.gateway.filter;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Limita la cantidad de peticiones por usuario (o por IP si todavía no hay JWT)
 * dentro de una ventana de tiempo. Corre después de JwtValidationFilter para
 * poder usar X-User-Id como clave cuando la request ya está autenticada.
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Value("${rate-limit.max-requests:100}")
    private int maxRequests;

    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${rate-limit.strict-max-requests:5}")
    private int strictMaxRequests;

    // Rutas sensibles a fuerza bruta / abuso: límite propio, más estricto que el general.
    private static final List<String> STRICT_EXACT_PATHS = List.of(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/ia/builder/chat"
    );

    private static final Pattern STORE_TOGGLE_STATUS_PATTERN =
            Pattern.compile("^/api/v1/stores/[^/]+/toggle-status$");

    private final Map<String, RequestCounter> counters = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        return -50;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if ("OPTIONS".equals(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        String path = request.getURI().getPath();
        boolean strict = isStrictPath(path);
        int limit = strict ? strictMaxRequests : maxRequests;
        String key = resolveKey(request) + (strict ? "|strict:" + path : "");

        long windowMillis = windowSeconds * 1000L;
        long now = System.currentTimeMillis();

        RequestCounter counter = counters.computeIfAbsent(key, k -> new RequestCounter(now));
        int count;
        synchronized (counter) {
            if (now - counter.windowStart > windowMillis) {
                counter.windowStart = now;
                counter.count = 0;
            }
            count = ++counter.count;
        }

        if (count > limit) {
            log.warn("Rate limit excedido | key={} | {} peticiones en {}s (límite={})", key, count, windowSeconds, limit);
            return tooManyRequests(exchange);
        }

        return chain.filter(exchange);
    }

    private boolean isStrictPath(String path) {
        return STRICT_EXACT_PATHS.contains(path) || STORE_TOGGLE_STATUS_PATTERN.matcher(path).matches();
    }

    private String resolveKey(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            return "user:" + userId;
        }
        InetSocketAddress remote = request.getRemoteAddress();
        String ip = (remote != null && remote.getAddress() != null)
                ? remote.getAddress().getHostAddress()
                : "unknown";
        return "ip:" + ip;
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(windowSeconds));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"status\":429,\"message\":\"Demasiadas peticiones, intenta de nuevo más tarde\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /** Limpia entradas inactivas para no crecer indefinidamente en memoria. */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanup() {
        long staleAfter = windowSeconds * 1000L * 2;
        long now = System.currentTimeMillis();
        counters.entrySet().removeIf(e -> now - e.getValue().windowStart > staleAfter);
    }

    private static class RequestCounter {
        volatile long windowStart;
        int count;

        RequestCounter(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
