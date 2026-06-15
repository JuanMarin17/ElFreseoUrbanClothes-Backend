package com.user.api.user.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSessionResponseDTO {
    private UUID id;
    private String device;
    private String browser;
    private String os;
    private String ipAddress;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastSeenAt;
    private OffsetDateTime expiresAt;
    private boolean active;
    private boolean current;
}
