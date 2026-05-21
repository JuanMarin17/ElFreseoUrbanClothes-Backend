package com.api.Supplier.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String contactName;
    private String phone;
    private String email;
}