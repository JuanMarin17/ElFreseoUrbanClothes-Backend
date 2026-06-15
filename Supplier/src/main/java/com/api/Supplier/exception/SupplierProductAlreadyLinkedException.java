package com.api.Supplier.exception;

public class SupplierProductAlreadyLinkedException extends RuntimeException {
    public SupplierProductAlreadyLinkedException(String message) {
        super(message);
    }
}
