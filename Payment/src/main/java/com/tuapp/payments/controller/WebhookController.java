package com.tuapp.payments.controller;

import com.tuapp.payments.config.MercadoPagoConfiguration;
import com.tuapp.payments.webhook.WebhookProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Recibe notificaciones IPN/Webhook de Mercado Pago.
 *
 * MP enviará POST a /api/payments/webhook con los campos:
 *   - id: ID de la notificación
 *   - topic/type: "payment", "preapproval", "merchant_order", etc.
 *   - data.id: ID del recurso (pago, suscripción, etc.)
 *
 * Headers importantes que MP incluye para validar autenticidad:
 *   - x-signature
 *   - x-request-id
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookProcessor webhookProcessor;
    private final MercadoPagoConfiguration mpConfig;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
        @RequestBody Map<String, Object> payload,
        @RequestHeader(value = "x-signature", required = false) String xSignature,
        @RequestHeader(value = "x-request-id", required = false) String xRequestId
    ) {
        log.info("Webhook recibido: {}", payload);

        // Validar firma HMAC si está configurado el secret
        if (mpConfig.getWebhookSecret() != null && !mpConfig.getWebhookSecret().isBlank()) {
            if (!isValidSignature(payload, xSignature, xRequestId)) {
                log.warn("Firma de webhook inválida");
                return ResponseEntity.status(401).build();
            }
        }

        String type  = getStringValue(payload, "type");
        String topic = getStringValue(payload, "topic");

        // Normalizar: MP usa "type" en nuevas versiones, "topic" en las antiguas
        String eventType = (type != null) ? type : topic;

        if ("payment".equalsIgnoreCase(eventType)) {
            String paymentId = extractPaymentId(payload);
            if (paymentId != null) {
                // Procesar en hilo aparte para responder rápido a MP (< 5s)
                Thread.ofVirtual().start(() -> {
                    try {
                        webhookProcessor.processPaymentNotification(paymentId);
                    } catch (Exception e) {
                        log.error("Error procesando webhook de pago {}", paymentId, e);
                    }
                });
            }
        } else if ("subscription_preapproval".equalsIgnoreCase(eventType)
                || "preapproval".equalsIgnoreCase(eventType)) {
            String preapprovalId = extractPaymentId(payload);
            if (preapprovalId != null) {
                Thread.ofVirtual().start(() -> {
                    try {
                        webhookProcessor.processSubscriptionNotification(preapprovalId);
                    } catch (Exception e) {
                        log.error("Error procesando webhook de suscripción {}", preapprovalId, e);
                    }
                });
            }
        } else {
            log.debug("Evento de webhook no manejado: {}", eventType);
        }

        // Siempre responder 200 a MP para evitar reintentos
        return ResponseEntity.ok().build();
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private String extractPaymentId(Map<String, Object> payload) {
        // Formato nuevo: {"data": {"id": "123456"}}
        if (payload.get("data") instanceof Map<?,?> data) {
            Object id = data.get("id");
            return id != null ? id.toString() : null;
        }
        // Formato antiguo: {"id": "123456"}
        Object id = payload.get("id");
        return id != null ? id.toString() : null;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    /**
     * Valida la firma HMAC-SHA256 que envía Mercado Pago en el header x-signature.
     * Formato del header: ts=TIMESTAMP,v1=HASH
     */
    private boolean isValidSignature(Map<String, Object> payload, String xSignature, String xRequestId) {
        if (xSignature == null || xSignature.isBlank()) return false;

        try {
            String ts = null;
            String v1 = null;

            for (String part : xSignature.split(",")) {
                String[] kv = part.trim().split("=", 2);
                if (kv.length == 2) {
                    if ("ts".equals(kv[0]))  ts = kv[1];
                    if ("v1".equals(kv[0]))  v1 = kv[1];
                }
            }

            if (ts == null || v1 == null) return false;

            String dataId = extractPaymentId(payload);
            // Cadena a firmar: id:DATAID;request-id:REQID;ts:TS;
            String manifest = "id:" + (dataId != null ? dataId : "") + ";"
                + "request-id:" + (xRequestId != null ? xRequestId : "") + ";"
                + "ts:" + ts + ";";

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                mpConfig.getWebhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexHash = new StringBuilder();
            for (byte b : hash) hexHash.append(String.format("%02x", b));

            return hexHash.toString().equals(v1);

        } catch (Exception e) {
            log.error("Error validando firma webhook", e);
            return false;
        }
    }
}
