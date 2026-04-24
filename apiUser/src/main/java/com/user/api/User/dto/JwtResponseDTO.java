package com.user.api.user.dto;

import lombok.Data;

@Data
public class JwtResponseDTO {
    private String message;
    private String jwt;
}
