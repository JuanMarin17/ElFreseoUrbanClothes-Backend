package com.user.api.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoResponseDTO {
    private UUID userId;
    private String email;
    private LocalDateTime createAt;
}
