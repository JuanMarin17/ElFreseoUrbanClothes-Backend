package com.api.Returns.exception;

public class ReturnNotFoundException extends RuntimeException {
    public ReturnNotFoundException(String message) {
        super(message);
    }
}