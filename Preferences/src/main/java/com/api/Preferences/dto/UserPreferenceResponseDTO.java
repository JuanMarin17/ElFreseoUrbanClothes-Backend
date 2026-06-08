package com.api.Preferences.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserPreferenceResponseDTO {
    private UUID preferenceId;
    private UUID userId;
    private String preferenceType;
    private String preferenceValue;
}