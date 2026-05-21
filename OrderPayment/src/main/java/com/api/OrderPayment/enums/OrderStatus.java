package com.api.OrderPayment.enums;

public enum OrderStatus {
    PENDING,        // Orden creada, esperando pago
    CONFIRMED,      // Pago confirmado
    PROCESSING,     // En preparación
    SHIPPED,        // Enviada
    DELIVERED,      // Entregada
    CANCELLED,      // Cancelada
    REFUNDED        // Reembolsada
}
