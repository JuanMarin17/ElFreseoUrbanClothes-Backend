
package com.api.Users.exception;

import com.api.Users.dto.MessageResponseDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Errores de @Valid — campos inválidos en el request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación", fieldErrors);
    }

    // Error si el jwt no contiene el userId, o sea no esta autenticado
    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedUser(UnauthorizedUserException e){
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    // Error si el id del usuario no corresponde con el nesecario
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException e){
        return buildResponse( HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    // Cualquier otro error no controlado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String , Object>> handleGeneric(Exception e) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno del servidor " + e, null);
    }

    // Builder del response estandarizado
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, Object errors) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);

        if (errors != null) {
            body.put("errors", errors);
        }

        return ResponseEntity.status(status).body(body);
    }
}
