package com.api.Cart.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) { super(message); }
}
