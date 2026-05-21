package com.api.Support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketRequestDTO {

    @NotBlank(message = "El asunto es obligatorio")
    private String subject;
}