package com.api.Preferences.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.api.Preferences.dto.UserBehaviorRequestDTO;
import com.api.Preferences.dto.UserBehaviorResponseDTO;
import com.api.Preferences.dto.UserPreferenceRequestDTO;
import com.api.Preferences.dto.UserPreferenceResponseDTO;
import com.api.Preferences.entity.UserBehavior;
import com.api.Preferences.entity.UserPreference;
import com.api.Preferences.exception.BadRequestException;
import com.api.Preferences.exception.UnauthorizedException;
import com.api.Preferences.repository.UserBehaviorRepository;
import com.api.Preferences.repository.UserPreferenceRepository;
import com.common_request_context_starter.context.RequestContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserBehaviorRepository behaviorRepository;

    // ── Obtener preferencias ──────────────────────────────────────────────────
    public List<UserPreferenceResponseDTO> getMyPreferences() {
        UUID userId = getUserIdFromHeader();
        return preferenceRepository.findByUserId(userId)
                .stream().map(this::toPreferenceResponse).toList();
    }

    // ── Guardar preferencia ───────────────────────────────────────────────────
    public UserPreferenceResponseDTO savePreference(UserPreferenceRequestDTO dto) {
        UUID userId = getUserIdFromHeader();

        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setPreferenceType(dto.getPreferenceType());
        preference.setPreferenceValue(dto.getPreferenceValue());

        return toPreferenceResponse(preferenceRepository.save(preference));
    }

    // ── Registrar comportamiento ──────────────────────────────────────────────
    public UserBehaviorResponseDTO trackBehavior(UserBehaviorRequestDTO dto) {
        UUID userId = getUserIdFromHeader();

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setEventType(dto.getEventType());
        behavior.setProductId(dto.getProductId());

        return toBehaviorResponse(behaviorRepository.save(behavior));
    }

    // ── Obtener comportamientos ───────────────────────────────────────────────
    public List<UserBehaviorResponseDTO> getMyBehaviors() {
        UUID userId = getUserIdFromHeader();
        return behaviorRepository.findByUserId(userId)
                .stream().map(this::toBehaviorResponse).toList();
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private UUID getUserIdFromHeader() {
        String userIdHeader = RequestContext.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank())
            throw new UnauthorizedException("Usuario no autenticado");
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Formato inválido del userId");
        }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────
    private UserPreferenceResponseDTO toPreferenceResponse(UserPreference p) {
        UserPreferenceResponseDTO dto = new UserPreferenceResponseDTO();
        dto.setPreferenceId(p.getPreferenceId());
        dto.setUserId(p.getUserId());
        dto.setPreferenceType(p.getPreferenceType());
        dto.setPreferenceValue(p.getPreferenceValue());
        return dto;
    }

    private UserBehaviorResponseDTO toBehaviorResponse(UserBehavior b) {
        UserBehaviorResponseDTO dto = new UserBehaviorResponseDTO();
        dto.setBehaviorId(b.getBehaviorId());
        dto.setUserId(b.getUserId());
        dto.setEventType(b.getEventType());
        dto.setProductId(b.getProductId());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }
}