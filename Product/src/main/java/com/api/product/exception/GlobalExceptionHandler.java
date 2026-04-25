package com.api.product.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.api.product.dto.ErrorResponseDTO;

/**
 * GLOBAL EXCEPTION HANDLER - Manejador Global de Excepciones
 * 
 * ¿PARA QUÉ SIRVE?
 * Intercepta todas las excepciones que ocurran en los controladores de la aplicación
 * y las convierte en respuestas HTTP estructuradas y amigables para el cliente.
 * En lugar de mostrar errores técnicos, devuelve JSON estructurado con información clara.
 * 
 * ¿CÓMO FUNCIONA?
 * @RestControllerAdvice: Permite que esta clase sea un manejador global de errores
 * para TODOS los controladores REST de la aplicación.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * MÉTODO 1: handleValidationException
     * 
     * ¿PARA QUÉ SIRVE?
     * Maneja los errores de validación cuando los datos enviados por el cliente
     * no cumplen con las reglas definidas en ProductRequestDTO (@NotBlank, @Size, etc).
     * 
     * EJEMPLO: Si envías un producto sin nombre (campo obligatorio),
     * este método captura el error y devuelve un JSON estructurado.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) // Escucha esta excepción específica
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,  // La excepción capturada
            WebRequest request) {                 // Información de la solicitud HTTP

        // Crear un Map para agrupar errores por nombre de campo
        // EJEMPLO: { "name_product": ["El nombre es obligatorio"], "sale_price": ["Precio inválido"] }
        Map<String, List<String>> errors = new HashMap<>();
        
        // forEach: Recorre cada error de validación encontrado
        // computeIfAbsent: Si el campo no existe en el Map, lo crea con una lista nueva
        // add: Agrega el mensaje del error a la lista del campo
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.computeIfAbsent(error.getField(), k -> new java.util.ArrayList<>())
                  .add(error.getDefaultMessage())
        );

        // Construir la respuesta de error usando el patrón Builder
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())                    // 400 - Solicitud incorrecta
                .message("Error de validación en los datos enviados")      // Mensaje amigable
                .path(request.getDescription(false).replace("uri=", ""))  // Ruta que causó el error (/products)
                .timestamp(LocalDateTime.now())                            // Hora del error
                .errors(errors)                                            // Map con errores por campo
                .exception("MethodArgumentNotValidException")             // Tipo de excepción
                .build();

        // Retornar ResponseEntity con estado 400 (BAD_REQUEST) y el JSON de error
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * MÉTODO 2: handleResourceNotFoundException
     * 
     * ¿PARA QUÉ SIRVE?
     * Maneja excepciones cuando se intenta acceder a un producto que no existe
     * (por ejemplo: GET /products/9999 donde 9999 no existe en la BD).
     * 
     * EJEMPLO: Buscas un producto con ID 999 que no existe,
     * este método captura el error y devuelve 404 con mensaje claro.
     */
    @ExceptionHandler(ResourceNotFoundException.class) // Escucha la excepción personalizada
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex,  // La excepción personalizada lanzada
            WebRequest request) {

        // Construir respuesta de error
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())                      // 404 - No encontrado
                .message(ex.getMessage())                                  // Mensaje del error
                .path(request.getDescription(false).replace("uri=", ""))  // Ruta que causó error
                .timestamp(LocalDateTime.now())                            // Hora del error
                .exception("ResourceNotFoundException")                    // Tipo de excepción
                .build();

        // Retornar ResponseEntity con estado 404 (NOT_FOUND)
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * MÉTODO 3: handleGlobalException
     * 
     * ¿PARA QUÉ SIRVE?
     * "Red de seguridad" - Captura CUALQUIER excepción no manejada
     * que no coincida con los métodos anteriores.
     * Previene que errores inesperados expongan información técnica.
     * 
     * EJEMPLO: Si ocurre un error en la base de datos o un null pointer
     * que no fue validado, este método lo atrapa.
     */
    @ExceptionHandler(Exception.class) // Escucha CUALQUIER excepción (la más genérica)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(
            Exception ex,                  // Cualquier excepción
            WebRequest request) {

        // Construir respuesta de error genérica
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())          // 500 - Error interno
                .message("Error interno del servidor")                     // Mensaje genérico (no expone detalles)
                .path(request.getDescription(false).replace("uri=", ""))  // Ruta que causó error
                .timestamp(LocalDateTime.now())                            // Hora del error
                .exception(ex.getClass().getSimpleName())                 // Tipo real de excepción (para logs)
                .build();

        // Retornar ResponseEntity con estado 500 (INTERNAL_SERVER_ERROR)
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
