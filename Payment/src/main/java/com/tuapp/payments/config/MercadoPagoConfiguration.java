package com.tuapp.payments.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MercadoPagoConfiguration {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.client-id}")
    private String clientId;

    @Value("${mercadopago.client-secret}")
    private String clientSecret;

    @Value("${mercadopago.webhook-secret}")
    private String webhookSecret;

    @Value("${mercadopago.platform-fee-ratio:0.01}")
    private Double platformFeeRatio;

    @Value("${mercadopago.redirect-uri}")
    private String redirectUri;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /** URL de autorización OAuth de Mercado Pago */
    public String getOAuthUrl() {
        return "https://auth.mercadopago.com.co/authorization"
            + "?client_id=" + clientId
            + "&response_type=code"
            + "&platform_id=mp"
            + "&redirect_uri=" + redirectUri;
    }

    @PostConstruct
    public void init() {
        // Inicializa el SDK con tu access token de plataforma
        MercadoPagoConfig.setAccessToken(accessToken);
    }
}
