package com.apiPreferences.dto;

import lombok.Data;

@Data

public class PreferencesRequestDTO {
    private Long user_id;
    private String sports;
    private String colors;
    private String preferences_size;
    private String clothing_style;
}
