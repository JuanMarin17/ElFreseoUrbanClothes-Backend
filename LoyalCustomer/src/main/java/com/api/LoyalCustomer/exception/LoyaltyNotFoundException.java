package com.api.LoyalCustomer.exception;

public class LoyaltyNotFoundException extends RuntimeException {
    public LoyaltyNotFoundException(String message) {
        super(message);
    }
}