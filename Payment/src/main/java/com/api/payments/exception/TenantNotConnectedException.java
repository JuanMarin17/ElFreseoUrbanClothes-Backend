package com.api.payments.exception;

public class TenantNotConnectedException extends RuntimeException {
    public TenantNotConnectedException(String tenantId) {
        super("El tenant '" + tenantId + "' no tiene cuenta Mercado Pago conectada. " +
              "Debe completar el flujo OAuth primero.");
    }
}
