package com.user.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "El correo no puede estar vació")
    @Email(message = "El correo no tiene un formato valido")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacia")
    private String password;
}
