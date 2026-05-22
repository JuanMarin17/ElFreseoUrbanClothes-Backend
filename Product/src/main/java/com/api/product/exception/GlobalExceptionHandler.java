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

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponseDTO> handleValidationException(
                        MethodArgumentNotValidException ex, WebRequest request) {

                Map<String, List<String>> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errors
                                                .computeIfAbsent(error.getField(), k -> new java.util.ArrayList<>())
                                                .add(error.getDefaultMessage()));

                return build(HttpStatus.BAD_REQUEST, "Error de validación en los datos enviados",
                                request, "MethodArgumentNotValidException", errors);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponseDTO> handleBadRequest(
                        BadRequestException ex, WebRequest request) {
                return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "BadRequestException", null);
        }

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ErrorResponseDTO> handleConflict(
                        ConflictException ex, WebRequest request) {
                return build(HttpStatus.CONFLICT, ex.getMessage(), request, "ConflictException", null);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponseDTO> handleNotFound(
                        ResourceNotFoundException ex, WebRequest request) {
                return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, "ResourceNotFoundException", null);
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponseDTO> handleUnauthorized(
                        UnauthorizedException ex, WebRequest request) {
                return build(HttpStatus.FORBIDDEN, ex.getMessage(), request, "UnauthorizedException", null);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponseDTO> handleNotReadable(
                        HttpMessageNotReadableException ex, WebRequest request) {
                return build(HttpStatus.BAD_REQUEST,
                                "El cuerpo de la solicitud está mal formado o contiene tipos inválidos",
                                request, "HttpMessageNotReadableException", null);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponseDTO> handleGlobal(
                        Exception ex, WebRequest request) {
                return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
                                request, ex.getClass().getSimpleName(), null);
        }

        private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message,
                        WebRequest request, String exception, Map<String, List<String>> errors) {

                ErrorResponseDTO body = ErrorResponseDTO.builder()
                                .status(status.value())
                                .message(message)
                                .path(request.getDescription(false).replace("uri=", ""))
                                .timestamp(LocalDateTime.now())
                                .exception(exception)
                                .errors(errors)
                                .build();

                return ResponseEntity.status(status).body(body);
        }
}