package com.api.gateway.filter;

import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
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

        // Siempre dejar pasar OPTIONS (preflight CORS)
        if ("OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }

        // Rutas públicas: no requieren JWT
        if (isPublicPath(path, method)) {
            return chain.filter(exchange);
        }

        long startTime = System.currentTimeMillis();
        String authHeader = request.getHeaders().getFirst("Authorization");
        String storeId = request.getHeaders().getFirst("X-Store-Id");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            log.warn("JWT Filter took {} ms (UNAUTHORIZED - no token) | Path: {}",
                    System.currentTimeMillis() - startTime, path);
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

            if (storeId != null)
                builder.header("X-Store-Id", storeId);

            ServerHttpRequest enrichedRequest = builder.build();

            return chain.filter(exchange.mutate().request(enrichedRequest).build())
                    .doFinally(signalType -> log.info("JWT Filter completed in {} ms | Path: {}",
                            System.currentTimeMillis() - startTime, path));

        } catch (Exception e) {
            log.error("Jwt validation failed in {} ms: {} | Path: {}",
                    System.currentTimeMillis() - startTime, e.getMessage(), path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path, String method) {
        // GET /api/v1/reviews exacto es público; /reviews/me y POST /reviews requieren JWT
        if ("GET".equals(method) && "/api/v1/reviews".equals(path)) return true;
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
