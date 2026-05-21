package com.api.OrderPayment.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Utilidad para leer headers inyectados por el gateway de autenticación.
 * Mismo patrón que el módulo Cart.
 */
@Component
@RequiredArgsConstructor
public class HeaderUtil {

    private final HttpServletRequest request;

    public Optional<UUID> getUserIdFromHeader() {
        String userId = request.getHeader("x-user-id");
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(userId));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El header 'x-user-id' no es un UUID válido: " + userId);
        }
    }
}
