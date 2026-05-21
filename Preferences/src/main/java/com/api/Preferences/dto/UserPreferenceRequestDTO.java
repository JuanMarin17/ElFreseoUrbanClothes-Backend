package com.api.Preferences.dto;

import lombok.Data;

@Data
public class UserPreferenceRequestDTO {
    private String preferenceType;
    private String preferenceValue;
}