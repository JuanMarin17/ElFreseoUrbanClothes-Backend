package com.api.Store.exception;

public class UserAlreadyInStoreException extends RuntimeException {
    public UserAlreadyInStoreException(String message) {
        super(message);
    }
}
