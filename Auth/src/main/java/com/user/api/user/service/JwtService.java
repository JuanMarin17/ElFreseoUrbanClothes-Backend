package com.user.api.user.service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${security.jwt.secret-key}")
    String secretKey;

    @Value("${security.jwt.token-expiration}")
    Long tokenExpiration;

    private SecretKey getSignKey() {
        byte[] keyBites = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBites);
    }

    /**
     * 
     * @param userId
     * @param first_name
     * @param last_name
     * @param rolId
     * @return
     */
    public String generateToken(UUID userId, String fullName, String rolId, String email, UUID sessionId) {
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("user_id", userId);
        claims.put("role", rolId);
        claims.put("email", email);
        if (sessionId != null) {
            claims.put("session_id", sessionId.toString());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(fullName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSignKey())
                .compact();
    }

    public UUID extractSessionId(String token) {
        return extractClaims(token, claims -> {
            String sessionId = claims.get("session_id", String.class);
            return sessionId != null ? UUID.fromString(sessionId) : null;
        });
    }

    /**
     * Funcion que permite validar si el token es valido
     * o es un token falso o invalido
     * 
     * @param token
     * @return
     */
    public Boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (JwtException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Funcion generica que nos permite extraer todos los
     * claims del usuario
     * 
     * @param <T>
     * @param token
     * @param resolver
     * @return
     */
    public <T> T extractClaims(String token, Function<Claims, T> resolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }

    /**
     * Extrate el nombre de usuario por medio del Subject
     * 
     * @param token
     * @return
     */
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    /**
     * Extrae el id del usuario
     * 
     * @param token
     * @return
     */
    public UUID extractUserId(String token) {
        return extractClaims(token, claims -> UUID.fromString(claims.get("user_id", String.class)));
    }

    /**
     * Extrae el id del rol
     * 
     * @param token
     * @return
     */
    public String extractRol(String token) {
        return extractClaims(token, claims -> claims.get("role", String.class));
    }

    /**
     * 
     * @param token
     * @return
     */
    public String refrechToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        } catch (JwtException e) {
            throw new RuntimeException("Token is invalid " + e.getMessage());
        }

        String rawSessionId = claims.get("session_id", String.class);
        UUID sessionId = rawSessionId != null ? UUID.fromString(rawSessionId) : null;
        return generateToken(UUID.fromString(claims.get("user_id", String.class)), claims.getSubject(), claims.get("role", String.class), claims.get("email", String.class), sessionId);
    }

}
