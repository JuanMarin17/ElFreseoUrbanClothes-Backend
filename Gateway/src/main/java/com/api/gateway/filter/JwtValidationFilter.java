package com.api.gateway.filter;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private final WebClient authWebClient;

    public JwtValidationFilter(@Qualifier("authWebClient") WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    // Patrón para el stream SSE del usuario: /api/v1/notifications/user/{userId}/stream
    private static final Pattern USER_STREAM_PATTERN =
            Pattern.compile("^/api/v1/notifications/user/([^/]+)/stream$");

    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/v1/auth/register",
        "/api/v1/auth/registerSecondStep",
        "/api/v1/auth/resendVerificationCode",
        "/api/v1/auth/login",
        "/api/v1/auth/loginSecondStep",
        "/api/v1/auth/forgotPassword",
        "/api/v1/auth/forgotPasswordSecondStep",
        "/api/v1/auth/deactivateAccount",
        "/api/v1/auth/refresh-token",
        "/api/v1/products/all/active",
        "/api/v1/promotions/getActivePromotions",
        "/api/v1/coupons/getActiveCoupons",
        "/api/v1/upload",
        "/api/v1/cloudinary",
        "/api/v1/alerts/stock/stream",
        "/api/v1/users/createUser",
        "/api/v1/oauth/callback",
        "/api/v1/payments/webhook",
        "/api/v1/transactions/plans",
        "/api/v1/transactions/limits",
        "/api/v1/transactions/webhook"
    );

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        if ("OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }

        if (isPublicPath(path, method)) {
            return chain.filter(exchange);
        }

        long startTime = System.currentTimeMillis();
        String authHeader = request.getHeaders().getFirst("Authorization");
        String storeId = request.getHeaders().getFirst("X-Store-Id");

        // Fallback ?token para endpoints donde el browser no puede enviar headers:
        //  · SSE (EventSource): /notifications/*/stream
        //  · Descarga de CSV:  /sales/export
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            boolean needsQueryToken = path.endsWith("/sales/export")
                    || (path.contains("/notifications/") && path.endsWith("/stream"));
            if (needsQueryToken) {
                String queryToken = request.getQueryParams().getFirst("token");
                if (queryToken != null && !queryToken.isBlank()) {
                    authHeader = "Bearer " + queryToken;
                }
            }
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            log.warn("JWT Filter (UNAUTHORIZED - no token) | Path: {}", path);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.replace("Bearer ", "");

        try {
            SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            ServerHttpRequest.Builder builder = request.mutate()
                    .header("Authorization", authHeader)
                    .header("X-User-Id", claims.get("user_id", String.class))
                    .header("X-User-Name", claims.getSubject())
                    .header("X-User-Role", claims.get("role", String.class))
                    .header("X-User-Email", claims.get("email", String.class));

            String sessionId = claims.get("session_id", String.class);
            if (sessionId != null) builder.header("X-Session-Id", sessionId);
            if (storeId != null) builder.header("X-Store-Id", storeId);

            ServerHttpRequest enrichedRequest = builder.build();
            ServerWebExchange enrichedExchange = exchange.mutate().request(enrichedRequest).build();

            // SSE stream del usuario: verificar que el userId del JWT coincide con el de la URL
            Matcher streamMatcher = USER_STREAM_PATTERN.matcher(path);
            if (streamMatcher.matches()) {
                String pathUserId = streamMatcher.group(1);
                String jwtUserId  = claims.get("user_id", String.class);
                String role       = claims.get("role", String.class);
                boolean isAdmin   = "ADMIN".equals(role) || "SUPERADMIN".equals(role);
                if (!pathUserId.equals(jwtUserId) && !isAdmin) {
                    log.warn("SSE ownership check failed | jwtUser={} pathUser={} path={}", jwtUserId, pathUserId, path);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            // Tokens sin session_id (emitidos antes del deploy): dejar pasar
            if (sessionId == null) {
                return chain.filter(enrichedExchange)
                        .doFinally(s -> log.info("JWT Filter {}ms (no session check) | {}", System.currentTimeMillis() - startTime, path));
            }

            // Tokens con session_id: verificar que la sesión siga activa en Auth
            return checkSessionActive(sessionId)
                    .flatMap(active -> {
                        if (!active) {
                            log.warn("JWT Filter (UNAUTHORIZED - session closed) | sessionId={} | Path={}", sessionId, path);
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(enrichedExchange)
                                .doFinally(s -> log.info("JWT Filter {}ms | {}", System.currentTimeMillis() - startTime, path));
                    });

        } catch (Exception e) {
            log.error("JWT validation failed in {}ms: {} | Path: {}", System.currentTimeMillis() - startTime, e.getMessage(), path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private Mono<Boolean> checkSessionActive(String sessionId) {
        return authWebClient.get()
                .uri("/auth/sessions/internal/" + sessionId + "/active")
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(2))
                .map(response -> true)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode().value() == 404) {
                        // Única señal real de sesión cerrada: Auth dice explícitamente que no existe
                        log.warn("Session {} closed or not found (404)", sessionId);
                        return Mono.just(false);
                    }
                    // 401, 403, 5xx u otros errores de Auth → fail open
                    log.error("Auth returned {} for session check, failing open", e.getStatusCode().value());
                    return Mono.just(true);
                })
                .onErrorResume(Exception.class, e -> {
                    // Timeout o Auth caído → fail open
                    log.error("Auth unreachable for session check (failing open): {}", e.getMessage());
                    return Mono.just(true);
                });
    }

    private boolean isPublicPath(String path, String method) {
        if ("GET".equals(method) && "/api/v1/reviews".equals(path)) return true;
        // Store endpoints públicos para la landing page (solo lectura)
        if ("GET".equals(method) && "/api/v1/stores".equals(path)) return true;
        if ("GET".equals(method) && path.startsWith("/api/v1/stores/getBySlug/")) return true;
        if ("GET".equals(method) && "/api/v1/stores/settings/getSettings".equals(path)) return true;
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
