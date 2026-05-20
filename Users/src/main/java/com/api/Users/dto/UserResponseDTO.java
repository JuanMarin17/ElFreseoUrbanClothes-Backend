package com.api.Users.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UserResponseDTO {
    private UUID userId;
    private String userName;
    private String userEmail;
    private String phone;
    private String imageProfile;
}
