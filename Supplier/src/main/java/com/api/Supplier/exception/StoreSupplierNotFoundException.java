package com.api.Supplier.exception;

import java.util.UUID;

public class StoreSupplierNotFoundException extends RuntimeException {
    public StoreSupplierNotFoundException(UUID storeId, UUID supplierId) {
        super("Relación no encontrada entre tienda " + storeId + " y supplier " + supplierId);
    }
}