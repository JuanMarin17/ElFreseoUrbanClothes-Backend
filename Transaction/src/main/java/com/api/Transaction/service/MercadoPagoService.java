package com.api.Transaction.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MercadoPagoService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    /**
     * Crea una preferencia de pago en Mercado Pago y devuelve la URL donde el OWNER
     * paga.
     *
     * @param planName    nombre del plan
     * @param amount      precio del plan
     * @param externalRef referencia interna (transactionId)
     * @return URL de pago de Mercado Pago
     */
    public String createPaymentPreference(String planName, BigDecimal amount, String externalRef) {
        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Vexio - Plan " + planName)
                    .quantity(1)
                    .unitPrice(amount)
                    .currencyId("COP")
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .externalReference(externalRef)
                    .notificationUrl(notificationUrl)
                    .backUrls(PreferenceBackUrlsRequest.builder()
                            .success("https://vexio.com/billing/success")
                            .failure("https://vexio.com/billing/failure")
                            .pending("https://vexio.com/billing/pending")
                            .build())
                    .autoReturn("approved")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);

            return preference.getInitPoint(); // URL de pago

        } catch (Exception e) {
            log.error("Error al crear preferencia en Mercado Pago: {}", e.getMessage());
            throw new RuntimeException("Error al conectar con Mercado Pago: " + e.getMessage());
        }
    }
}