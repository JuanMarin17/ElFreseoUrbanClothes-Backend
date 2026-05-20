package com.api.Supplier.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SupplierNotFoundException.class)
    public ProblemDetail handleSupplierNotFound(SupplierNotFoundException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, ex.getMessage(), "supplier-not-found");
    }

    @ExceptionHandler(StoreSupplierNotFoundException.class)
    public ProblemDetail handleStoreSupplierNotFound(StoreSupplierNotFoundException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, ex.getMessage(), "store-supplier-not-found");
    }

    @ExceptionHandler(StoreSupplierAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(StoreSupplierAlreadyExistsException ex) {
        return buildProblem(HttpStatus.CONFLICT, ex.getMessage(), "store-supplier-conflict");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "inválido");
        }

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        problem.setType(URI.create("/errors/validation"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", "internal-error");
    }

    private ProblemDetail buildProblem(HttpStatus status, String detail, String errorCode) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("/errors/" + errorCode));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}