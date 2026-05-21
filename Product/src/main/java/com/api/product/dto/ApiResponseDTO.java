package com.api.product.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO estándar para respuestas de la API.
 * Permite devolver mensaje, datos opcionales y fecha de la respuesta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDTO<T> {

    /** Mensaje descriptivo de la operación. */
    private String message;

    /** Código HTTP opcional, útil para el frontend. */
    private int status;

    /** Datos opcionales devueltos por la operación (ej. ProductResponseDTO). */
    private T data;

    /** Fecha y hora de la respuesta. */
    private OffsetDateTime timestamp;
}