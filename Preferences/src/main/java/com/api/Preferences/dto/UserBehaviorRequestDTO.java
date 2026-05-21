package com.api.Preferences.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserBehaviorRequestDTO {
    private String eventType;
    private UUID productId;
}