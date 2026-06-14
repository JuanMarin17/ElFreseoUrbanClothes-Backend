package com.api.gateway.filter;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtValidationFilter extends AbstractGatewayFilterFactory<Object> {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            long startTime = System.currentTimeMillis(); // ⏱️ inicio

            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst("Authorization");
            String storeId = request.getHeaders().getFirst("X-Store-Id");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                log.warn("JWT Filter took {} ms (UNAUTHORIZED - no token)",
                        System.currentTimeMillis() - startTime);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.replace("Bearer ", "");

            try {
                SecretKey key = Keys.hmacShaKeyFor(
                        Base64.getDecoder().decode(secretKey));

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

                if (storeId != null)
                    builder.header("X-Store-Id", storeId);

                ServerHttpRequest enrichedRequest = builder.build();

                return chain.filter(exchange.mutate().request(enrichedRequest).build())
                        .doFinally(signalType -> {
                            long duration = System.currentTimeMillis() - startTime;
                            log.info("JWT Filter completed in {} ms | Path: {}",
                                    duration,
                                    request.getURI().getPath());
                        });

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("Jwt validation failed in {} ms: {}", duration, e.getMessage());

                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }
}
