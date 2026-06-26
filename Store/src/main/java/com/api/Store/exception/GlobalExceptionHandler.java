package com.api.Store.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackages = "com.api.Store.controller")
public class GlobalExceptionHandler {

    // ── Validaciones Jakarta (@NotBlank, @Size, @Valid, etc.) ─────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación", fieldErrors);
    }

    // ── Tienda ya existe ──────────────────────────────────────────────────────
    @ExceptionHandler(StoreAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleStoreAlreadyExists(StoreAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // ── Tienda no encontrada ──────────────────────────────────────────────────
    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleStoreNotFound(StoreNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // ── Rol inválido ──────────────────────────────────────────────────────────
    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRole(InvalidRoleException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // ── Usuario ya pertenece a la tienda ──────────────────────────────────────
    @ExceptionHandler(UserAlreadyInStoreException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyInStore(UserAlreadyInStoreException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // ── Settings inválidas ────────────────────────────────────────────────────
    @ExceptionHandler(InvalidStoreSettingsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStoreSettings(InvalidStoreSettingsException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null);
    }

    // ── Acción no autorizada (rol insuficiente) ──────────────────────────────
    @ExceptionHandler(UnauthorizedStoreActionException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedStoreAction(UnauthorizedStoreActionException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    // ── Error genérico ────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Error no controlado [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, Object errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        if (errors != null)
            body.put("errors", errors);
        return ResponseEntity.status(status).body(body);
    }
}
