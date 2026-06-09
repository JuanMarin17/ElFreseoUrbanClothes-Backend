package com.api.Customer.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HeaderUtil {

    private final HttpServletRequest request;

    public Optional<UUID> getUserIdFromHeader() {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(userId));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El header 'X-User-Id' no es un UUID válido: " + userId);
        }
    }

    public UUID requireUserId() {
        return getUserIdFromHeader()
                .orElseThrow(() -> new IllegalArgumentException("El header 'X-User-Id' es obligatorio"));
    }
}
