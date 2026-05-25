package com.api.LoyalCustomer.enums;

public enum LedgerType {
    EARN, // — cuando el usuario gana puntos por hacer una compra
    REDEEM, // — cuando el usuario canjea sus puntos por un descuento
    EXPIRE // — cuando los puntos se vencen por pasar los 90 días sin usarlos
}