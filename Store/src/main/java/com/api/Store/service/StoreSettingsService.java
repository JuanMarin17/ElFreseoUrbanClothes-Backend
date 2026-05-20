package com.api.Store.service;

import com.api.Store.dto.settings.StoreSettingsRequestDTO;
import com.api.Store.dto.settings.StoreSettingsResponseDTO;
import com.api.Store.entity.Store;
import com.api.Store.entity.StoreSettings;
import com.api.Store.exception.StoreNotFoundException;
import com.api.Store.repository.StoreRepository;
import com.api.Store.repository.StoreSettingsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreSettingsService {

    private final StoreSettingsRepository storeSettingsRepository;
    private final StoreRepository storeRepository;
    private final ObjectMapper objectMapper;

    // ── 1. Obtener settings de una tienda ────────────────────────────────────
    public StoreSettingsResponseDTO getSettings(UUID storeId) {
        verifyStoreExists(storeId);

        StoreSettings settings = storeSettingsRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(
                        "La tienda con id " + storeId + " aún no tiene configuración"));

        return toResponse(settings);
    }

    // ── 2. Guardar / actualizar settings ─────────────────────────────────────
    @Transactional
    public StoreSettingsResponseDTO saveSettings(UUID storeId, StoreSettingsRequestDTO dto) {
        verifyStoreExists(storeId);

        StoreSettings settings = storeSettingsRepository.findById(storeId)
                .orElseGet(() -> {
                    Store store = storeRepository.findById(storeId)
                            .orElseThrow(() -> new StoreNotFoundException(
                                    "Tienda no encontrada con id: " + storeId));
                    return StoreSettings.builder().store(store).build();
                });

        applyChanges(settings, dto);
        settings.setUpdatedAt(OffsetDateTime.now());

        return toResponse(storeSettingsRepository.save(settings));
    }
    // ── Mapper ───────────────────────────────────────────────────────────────

    /**
     * Aplica solo los campos no nulos del DTO sobre la entidad (PATCH semántico).
     */
    private void applyChanges(StoreSettings settings, StoreSettingsRequestDTO dto) {
        if (dto.getCompletedStep() != null)
            settings.setCompletedStep(dto.getCompletedStep());

        if (dto.getLogoUrl() != null)
            settings.setLogoUrl(dto.getLogoUrl());

        if (dto.getPlan() != null)
            settings.setPlan(toMap(dto.getPlan()));

        if (dto.getBasic() != null) {
            Map<String, Object> basicMap = toMap(dto.getBasic());
            // Si el frontend manda logoPreview, lo usamos como logoUrl si no vino por separado
            if (dto.getLogoUrl() == null && basicMap.containsKey("logoPreview"))
                settings.setLogoUrl((String) basicMap.get("logoPreview"));
            settings.setBasic(basicMap);
        }

        if (dto.getComponents() != null)
            settings.setComponents(toMap(dto.getComponents()));

        if (dto.getLayout() != null)
            settings.setLayout(toMap(dto.getLayout()));

        if (dto.getLegal() != null)
            settings.setLegal(toMap(dto.getLegal()));

        if (dto.getPayment() != null)
            settings.setPayment(toMap(dto.getPayment()));

        if (dto.getStyles() != null)
            settings.setStyles(dto.getStyles());
    }

    private StoreSettingsResponseDTO toResponse(StoreSettings s) {
        return StoreSettingsResponseDTO.builder()
                .storeId(s.getStoreId())
                .completedStep(s.getCompletedStep())
                .logoUrl(s.getLogoUrl())
                .plan(s.getPlan())
                .basic(s.getBasic())
                .components(s.getComponents())
                .layout(s.getLayout())
                .legal(s.getLegal())
                .payment(s.getPayment())
                .styles(s.getStyles())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void verifyStoreExists(UUID storeId) {
        if (!storeRepository.existsById(storeId))
            throw new StoreNotFoundException("Tienda no encontrada con id: " + storeId);
    }

    /**
     * Convierte cualquier objeto (DTO anidado o Map) a Map<String, Object>
     * usando Jackson para evitar casteos manuales frágiles.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }
}
