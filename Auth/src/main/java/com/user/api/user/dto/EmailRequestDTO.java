package com.user.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailRequestDTO {
    @NotBlank(message = "El correo no puede estar vació")
    @Email(message = "El correo no tiene formato valido")
    private String email;
}
