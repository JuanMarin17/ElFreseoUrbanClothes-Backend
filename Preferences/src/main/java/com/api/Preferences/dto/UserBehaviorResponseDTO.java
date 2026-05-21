package com.api.Preferences.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class UserBehaviorResponseDTO {
    private UUID behaviorId;
    private UUID userId;
    private String eventType;
    private UUID productId;
    private OffsetDateTime createdAt;
}