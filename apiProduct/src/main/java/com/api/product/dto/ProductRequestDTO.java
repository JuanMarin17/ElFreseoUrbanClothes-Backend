package com.api.product.dto;

import lombok.Data;

import jakarta.validation.constraints.*;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name_product;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
    private String description_product;

    @NotBlank(message = "El tamaño del producto es obligatorio")
    @Pattern(
        regexp = "^(XS|S|M|L|XL|XXL)$",
        message = "El tamaño debe ser XS, S, M, L, XL o XXL"
    )
    private String size_product;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido")
    private Double sale_price_product;

    @NotNull(message = "El precio del proveedor es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio proveedor debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido")
    private Double supplier_price_product;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Max(value = 100000, message = "Stock demasiado alto")
    private Long stock_product;

    @NotNull(message = "La categoría es obligatoria")
    @Positive(message = "El id de categoría debe ser válido")
    private Long id_category;

    @NotNull(message = "El estado del producto es obligatorio")
    @Min(value = 0, message = "Estado inválido")
    @Max(value = 1, message = "El estado solo puede ser 0 (inactivo) o 1 (activo)")
    private Long state_product;
}