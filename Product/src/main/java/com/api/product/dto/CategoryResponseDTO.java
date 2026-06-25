package com.api.product.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {
    private UUID categoryId;
    private String name;
    private String status;

    // Atributos de variante configurables (null si la categoría no los personalizó)
    private String attribute1Label;
    private List<String> attribute1Options;
    private String attribute2Label;
    private List<String> attribute2Options;
    private Boolean attribute2IsColor;
}