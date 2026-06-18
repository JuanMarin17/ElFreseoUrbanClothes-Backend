package com.api.Support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketRequestDTO {

    @NotBlank(message = "El asunto es obligatorio")
    private String subject;

    private UUID storeId;
}