package com.api.Users.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {
    @NotNull(message = "El id del usuario es obligatorio")
    private UUID userId;

    @NotBlank(message = "El nombre de usuario no puede estar vació")
    private String userName;

    /** Opcional: los usuarios creados vía Google Sign-In no aportan teléfono. */
    private String phone;

    private String imageProfile;
}
