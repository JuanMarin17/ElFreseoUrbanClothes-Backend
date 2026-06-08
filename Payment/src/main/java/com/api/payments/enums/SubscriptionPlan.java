package com.api.payments.enums;

import java.math.BigDecimal;

public enum SubscriptionPlan {
    BASIC("Plan Básico",    new BigDecimal("29900")),  // precio en COP centavos
    PRO  ("Plan Pro",       new BigDecimal("79900")),
    ENTERPRISE("Enterprise",new BigDecimal("199900"));

    private final String displayName;
    private final BigDecimal priceInCents; // MP usa centavos

    SubscriptionPlan(String displayName, BigDecimal priceInCents) {
        this.displayName = displayName;
        this.priceInCents = priceInCents;
    }

    public String getDisplayName() { return displayName; }

    /** Precio que se manda a MP (en centavos) */
    public BigDecimal getPriceInCents() { return priceInCents; }

    /** Precio legible en pesos */
    public BigDecimal getPriceInPesos() { return priceInCents.divide(new BigDecimal("100")); }
}
