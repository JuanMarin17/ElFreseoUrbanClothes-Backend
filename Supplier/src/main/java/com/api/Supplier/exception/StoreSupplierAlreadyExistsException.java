package com.api.Supplier.exception;

import java.util.UUID;

public class StoreSupplierAlreadyExistsException extends RuntimeException {
    public StoreSupplierAlreadyExistsException(UUID storeId, UUID supplierId) {
        super("El supplier " + supplierId + " ya está asignado a la tienda " + storeId);
    }
}