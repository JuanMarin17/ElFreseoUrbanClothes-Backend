package com.user.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {
    @NotBlank(message = "EL codigo de verificación no puede estar vació")
    private String code;

    @NotBlank(message = "El correo electronico no puede estar vació")
    @Email(message = "Formato de correo invalido")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacia")
    private String password;
}
