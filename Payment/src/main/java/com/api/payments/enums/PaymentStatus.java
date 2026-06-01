package com.api.payments.enums;

// ─── Estado de un pago ───────────────────────────────────────────────────────
public enum PaymentStatus {
    PENDING,       // creado pero sin confirmar
    APPROVED,      // aprobado por MP
    REJECTED,      // rechazado
    CANCELLED,     // cancelado
    REFUNDED,      // reembolsado
    IN_PROCESS,    // en proceso (ej. efectivo pendiente)
    AUTHORIZED     // autorizado pero no capturado
}
