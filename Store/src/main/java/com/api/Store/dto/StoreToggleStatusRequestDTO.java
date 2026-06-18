package com.api.Store.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreToggleStatusRequestDTO {

    @NotNull(message = "isActive es obligatorio")
    private Boolean isActive;

    private String reason;
}
