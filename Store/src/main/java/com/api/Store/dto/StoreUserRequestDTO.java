package com.api.Store.dto;

import com.api.Store.enums.StoreRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StoreUserRequestDTO {

    @NotNull(message = "El id del usuario no puede estar vacío")
    private UUID userId;

    @NotNull(message = "El id de la tienda no puede estar vacío")
    private UUID storeId;

    @NotNull(message = "El rol no puede estar vacío")
    private StoreRole role;
}
