package com.api.Users.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank(message = "El id del usuario debe ser obligatorio")
    private UUID userId;

    @NotBlank(message = "El nombre de usuario no puede estar vació")
    private String userName;

    @NotBlank(message = "El telefono no puede estar vació")
    private String phone;

    private String imageProfile;
}
