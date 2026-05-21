package com.api.Support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequestDTO {

    @NotBlank(message = "El mensaje es obligatorio")
    private String message;
}