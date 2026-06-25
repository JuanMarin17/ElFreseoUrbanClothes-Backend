package com.api.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDTO {
    private String name;
    private Boolean active;

    // Atributos de variante configurables (opcional, se editan tras crear la categoría)
    private String attribute1Label;
    private List<String> attribute1Options;
    private String attribute2Label;
    private List<String> attribute2Options;
    private Boolean attribute2IsColor;
}