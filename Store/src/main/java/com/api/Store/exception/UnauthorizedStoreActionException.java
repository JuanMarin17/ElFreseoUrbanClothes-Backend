package com.api.Store.exception;

public class UnauthorizedStoreActionException extends RuntimeException {
    public UnauthorizedStoreActionException(String message) {
        super(message);
    }
}
