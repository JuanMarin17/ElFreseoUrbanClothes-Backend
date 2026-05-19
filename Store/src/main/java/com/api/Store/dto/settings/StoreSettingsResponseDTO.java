package com.api.Store.dto.settings;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Respuesta al obtener o guardar la configuración de una tienda.
 */
@Data
@Builder
public class StoreSettingsResponseDTO {

    private UUID storeId;
    private Integer completedStep;
    private String logoUrl;

    private Map<String, Object> plan;
    private Map<String, Object> basic;
    private Map<String, Object> components;
    private Map<String, Object> layout;
    private Map<String, Object> legal;
    private Map<String, Object> payment;
    private Map<String, Object> preview;
    private Map<String, Object> styles;

    private OffsetDateTime updatedAt;
}
