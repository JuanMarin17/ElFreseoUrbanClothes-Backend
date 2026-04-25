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
    public String generateToken(UUID userId, String fullName, UUID rolId) {

        return Jwts.builder()
                .claims(Map.of(
                        "user_id", userId,
                        "rol_id", rolId)) // Playload
                .subject(fullName) // Quien es el usuario
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSignKey()) // Firma digital
                .compact(); // Convierte al string final
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
        return extractClaims(token, claims -> claims.get("user_id", UUID.class));
    }

    /**
     * Extrae el id del rol
     * 
     * @param token
     * @return
     */
    public UUID extractRolId(String token) {
        return extractClaims(token, claims -> claims.get("rol_id", UUID.class));
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

        return generateToken(claims.get("user_id", UUID.class), claims.getSubject(), claims.get("rol_id", UUID.class));
    }

}
