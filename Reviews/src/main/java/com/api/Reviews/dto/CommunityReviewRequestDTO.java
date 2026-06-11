package com.api.Reviews.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommunityReviewRequestDTO {

    @NotNull(message = "El rating es obligatorio")
    @Min(value = 1, message = "El rating mínimo es 1")
    @Max(value = 5, message = "El rating máximo es 5")
    private Integer rating;

    @NotBlank(message = "El texto es obligatorio")
    @Size(min = 10, max = 300, message = "El texto debe tener entre 10 y 300 caracteres")
    private String text;
}
