package com.api.Store.dto.settings;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class StoreSettingsResponseDTO {
    private UUID storeId;
    private String logoUrl;
    private String bannerUrl;
    private String primaryColor;
    private String secondaryColor;
    private String font;
    private String theme;
    private Map<String, Object> layout;
    private String currency;
    private String language;
    private OffsetDateTime updatedAt;
}
