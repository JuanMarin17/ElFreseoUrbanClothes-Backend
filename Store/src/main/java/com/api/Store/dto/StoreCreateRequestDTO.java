package com.api.Store.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class StoreCreateRequestDTO {

    @NotNull(message = "El id del dueño no puede estar vacío")
    private UUID ownerId;

    @NotBlank(message = "El nombre del negocio no puede estar vacío")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String name;

    @NotBlank(message = "La url del negocio no puede estar vacía")
    @Size(min = 3, max = 80, message = "El slug debe tener entre 3 y 80 caracteres")
    private String slug;

    @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
    private String description;

    @Valid
    private StoreCmsDTO cms;
}
