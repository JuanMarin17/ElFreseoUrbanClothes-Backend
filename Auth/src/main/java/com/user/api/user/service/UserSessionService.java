package com.user.api.user.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.common_request_context_starter.context.RequestContext;
import com.user.api.user.dto.UserSessionResponseDTO;
import com.user.api.user.entity.UserSession;
import com.user.api.user.exception.UserNotFoundException;
import com.user.api.user.repository.UserSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;

    @Value("${security.jwt.token-expiration}")
    private Long tokenExpiration;

    public void saveSession(UUID userId, String ipAddress, String userAgent) {
        OffsetDateTime now = OffsetDateTime.now();

        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setIpAddress(ipAddress);
        session.setDevice(detectDevice(userAgent));
        session.setBrowser(detectBrowser(userAgent));
        session.setOs(detectOs(userAgent));
        session.setCreatedAt(now);
        session.setLastSeenAt(now);
        session.setExpiresAt(now.plusSeconds(tokenExpiration / 1000));
        session.setActive(true);

        userSessionRepository.save(session);
    }

    public List<UserSessionResponseDTO> getActiveSessions(UUID userId) {
        String requestingId = RequestContext.getHeader("X-User-Id");
        if (requestingId == null || !UUID.fromString(requestingId).equals(userId)) {
            throw new RuntimeException("No tienes permiso para ver estas sesiones");
        }

        return userSessionRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(s -> UserSessionResponseDTO.builder()
                        .id(s.getId())
                        .device(s.getDevice())
                        .browser(s.getBrowser())
                        .os(s.getOs())
                        .ipAddress(s.getIpAddress())
                        .createdAt(s.getCreatedAt())
                        .lastSeenAt(s.getLastSeenAt())
                        .expiresAt(s.getExpiresAt())
                        .active(s.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    public void deactivateSession(UUID sessionId) {
        String requestingId = RequestContext.getHeader("X-User-Id");
        if (requestingId == null) {
            throw new UserNotFoundException("Usuario no autenticado");
        }

        UUID requestingUserId = UUID.fromString(requestingId);

        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        if (!session.getUserId().equals(requestingUserId)) {
            throw new RuntimeException("No tienes permiso para cerrar esta sesión");
        }

        session.setActive(false);
        userSessionRepository.save(session);
    }

    private String detectDevice(String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return "Mobile";
        if (ua.contains("tablet") || ua.contains("ipad")) return "Tablet";
        return "Desktop";
    }

    private String detectBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/")) return "Edge";
        if (ua.contains("opr/") || ua.contains("opera")) return "Opera";
        if (ua.contains("chrome")) return "Chrome";
        if (ua.contains("firefox")) return "Firefox";
        if (ua.contains("safari")) return "Safari";
        return "Unknown";
    }

    private String detectOs(String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os")) return "macOS";
        if (ua.contains("android")) return "Android";
        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
        if (ua.contains("linux")) return "Linux";
        return "Unknown";
    }
}
