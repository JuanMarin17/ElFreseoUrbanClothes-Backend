package com.api.Supplier.exception;

import java.util.UUID;

public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(UUID supplierId) {
        super("Supplier no encontrado con id: " + supplierId);
    }

    public SupplierNotFoundException(String message) {
        super(message);
    }
}