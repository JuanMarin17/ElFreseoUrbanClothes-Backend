package com.api.Store.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@Component
public class HeaderUtil {

    /**
     * Extrae el header "x-store-id" de la request actual.
     * Retorna Optional vacío si el header no está presente o no es un UUID válido.
     */
    public Optional<UUID> getStoreIdFromHeader() {
        return getHeader("x-store-id")
                .flatMap(this::parseUUID);
    }

    /**
     * Extrae el header "x-user-id" de la request actual.
     * Útil para identificar al usuario autenticado sin JWT middleware.
     */
    public Optional<UUID> getUserIdFromHeader() {
        return getHeader("x-user-id")
                .flatMap(this::parseUUID);
    }

    /** Extrae cualquier header por nombre de forma segura */
    public Optional<String> getHeader(String headerName) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return Optional.empty();

        HttpServletRequest request = attrs.getRequest();
        return Optional.ofNullable(request.getHeader(headerName));
    }

    private Optional<UUID> parseUUID(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
