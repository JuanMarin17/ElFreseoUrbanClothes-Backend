package com.api.Users.client.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class AuthUserInfoDTO {
    private UUID userId;
    private String email;
    private LocalDateTime createAt;
}
