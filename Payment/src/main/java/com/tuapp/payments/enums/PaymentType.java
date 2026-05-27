package com.tuapp.payments.enums;

public enum PaymentType {
    SUBSCRIPTION,       // pago de plan hacia la plataforma
    STORE_SALE,         // venta de un producto de una tienda (split)
    STORE_SALE_PRO,     // venta con Checkout Pro
    STORE_SALE_API      // venta con Checkout API (tarjeta directo)
}
