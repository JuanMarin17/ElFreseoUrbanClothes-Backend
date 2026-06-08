package com.user.api.user.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private UUID userId;
    private String userName;
    private String phone;
    private String imageProfile;
}
