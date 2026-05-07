package com.api.product.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.api.product.dto.ErrorResponseDTO;

/**
 * GLOBAL EXCEPTION HANDLER - Manejador Global de Excepciones
 *
 * <p>
 * ¿PARA QUÉ SIRVE?
 * </p>
 * <p>
 * Esta clase se encarga de capturar todas las excepciones que ocurren dentro
 * de la aplicación (principalmente en los controllers) y convertirlas en
 * respuestas HTTP con formato JSON estructurado.
 * </p>
 *
 * <p>
 * ¿POR QUÉ ES IMPORTANTE?
 * </p>
 * <ul>
 *   <li>Evita que el backend devuelva errores técnicos (stack trace) al cliente.</li>
 *   <li>Centraliza el manejo de errores (no se repite código en cada controller).</li>
 *   <li>Permite retornar códigos HTTP correctos: 400, 404, 409, 500, etc.</li>
 *   <li>Mejora la comunicación con el frontend (el frontend sabe interpretar errores).</li>
 * </ul>
 *
 * <p>
 * ¿CÓMO FUNCIONA?
 * </p>
 * <p>
 * @RestControllerAdvice hace que esta clase intercepte errores de TODOS los controladores REST.
 * Cada método anotado con @ExceptionHandler escucha un tipo de excepción específico.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * MÉTODO 1: handleValidationException
     *
     * <p>
     * ¿PARA QUÉ SIRVE?
     * </p>
     * <p>
     * Captura errores cuando el cliente envía datos inválidos en un DTO,
     * por ejemplo cuando se usa @Valid en un controller y el DTO tiene anotaciones como:
     * @NotBlank, @NotNull, @Min, etc.
     * </p>
     *
     * <p>
     * EJEMPLO:
     * </p>
     * <pre>
     * {
     *   "name": "",
     *   "variants": []
     * }
     * </pre>
     *
     * <p>
     * Esto provocará errores que serán capturados aquí y se devolverá un JSON con detalles.
     * </p>
     *
     * @param ex      excepción lanzada por validaciones de Spring
     * @param request información de la petición HTTP
     * @return ResponseEntity con estado 400 y cuerpo JSON estructurado
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        // Mapa para agrupar errores por campo
        // Ejemplo:
        // {
        //   "name": ["El nombre es obligatorio"],
        //   "variants[0].sku": ["El SKU es obligatorio"]
        // }
        Map<String, List<String>> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.computeIfAbsent(error.getField(), k -> new java.util.ArrayList<>())
                        .add(error.getDefaultMessage())
        );

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Error de validación en los datos enviados")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .exception("MethodArgumentNotValidException")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * MÉTODO 2: handleBadRequestException
     *
     * <p>
     * ¿PARA QUÉ SIRVE?
     * </p>
     * <p>
     * Captura errores personalizados relacionados con peticiones incorrectas (400),
     * por ejemplo:
     * - datos faltantes
     * - reglas de negocio inválidas
     * - valores negativos donde no deberían existir
     * </p>
     *
     * @param ex      excepción personalizada BadRequestException
     * @param request información de la petición HTTP
     * @return ResponseEntity con estado 400
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequestException(
            BadRequestException ex,
            WebRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .exception("BadRequestException")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * MÉTODO 3: handleConflictException
     *
     * <p>
     * ¿PARA QUÉ SIRVE?
     * </p>
     * <p>
     * Maneja errores cuando hay un conflicto en el sistema (409).
     * Por ejemplo:
     * - intentar crear un producto con el mismo nombre
     * - intentar registrar una variante con SKU duplicado
     * </p>
     *
     * @param ex      excepción personalizada ConflictException
     * @param request información de la petición HTTP
     * @return ResponseEntity con estado 409
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflictException(
            ConflictException ex,
            WebRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .exception("ConflictException")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * MÉTODO 4: handleResourceNotFoundException
     *
     * <p>
     * ¿PARA QUÉ SIRVE?
     * </p>
     * <p>
     * Maneja errores cuando un recurso no existe en la base de datos.
     * Por ejemplo:
     * - buscar un producto por ID que no existe
     * - buscar una marca que no existe
     * - buscar una categoría que no existe
     * </p>
     *
     * @param ex      excepción personalizada ResourceNotFoundException
     * @param request información de la petición HTTP
     * @return ResponseEntity con estado 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .exception("ResourceNotFoundException")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * MÉTODO 5: handleHttpMessageNotReadableException
     *
     * <p>
     * ¿PARA QUÉ SIRVE?
     * </p>
     * <p>
     * Captura errores cuando el cliente envía un JSON mal formado o ilegible.
     * Esto pasa por ejemplo cuando el JSON está incompleto, tiene comas incorrectas,
     * o cuando se envía un tipo de dato inválido.
     * </p>
     *
     * <p>
     * EJEMPLO:
     * </p>
     * <pre>
     * {
     *   "price": "texto"   // debería ser número
     * }
     * </pre>
     *
     * @param ex      excepción lanzada por Spring cuando no puede leer el body
     * @param request información de la petición HTTP
     * @return ResponseEntity con estado 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("El cuerpo de la solicitud está mal formado o contiene tipos inválidos")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .exception("HttpMessageNotReadableException")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * MÉTODO 6: handleGlobalException
     *
     * <p>
     * ¿PARA QUÉ SIRVE?
     * </p>
     * <p>
     * Este método es la "red de seguridad" del sistema.
     * Captura cualquier excepción que no haya sido manejada por los métodos anteriores.
     * </p>
     *
     * <p>
     * IMPORTANTE:
     * </p>
     * <ul>
     *   <li>Devuelve 500 (INTERNAL_SERVER_ERROR)</li>
     *   <li>No expone detalles técnicos al cliente</li>
     *   <li>Evita que el backend devuelva mensajes peligrosos</li>
     * </ul>
     *
     * @param ex      excepción general
     * @param request información de la petición HTTP
     * @return ResponseEntity con estado 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(
            Exception ex,
            WebRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Error interno del servidor")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .exception(ex.getClass().getSimpleName())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}