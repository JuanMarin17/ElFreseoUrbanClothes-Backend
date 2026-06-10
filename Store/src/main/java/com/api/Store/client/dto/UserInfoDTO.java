package com.api.Store.client.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserInfoDTO {
    private UUID userId;
    private String userName;
    private String userEmail;
    private String phone;
    private String imageProfile;
}
