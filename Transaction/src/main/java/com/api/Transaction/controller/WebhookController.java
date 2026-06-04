package com.api.Transaction.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.Transaction.dto.WebhookNotificationDTO;
import com.api.Transaction.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/transactions/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final TransactionService transactionService;

    /**
     * MercadoPago envía notificaciones aquí cuando un pago cambia de estado.
     * POST /api/v1/transactions/webhook
     * No requiere JWT — MP no lo manda.
     */
    @PostMapping
    public ResponseEntity<Void> webhook(
            @RequestBody WebhookNotificationDTO notification,
            @RequestParam(required = false) Map<String, String> params) {

        log.info("Webhook MP recibido: type={}", notification.getType());

        if ("payment".equals(notification.getType()) && notification.getData() != null) {
            String mpPaymentId = notification.getData().getId();
            String externalRef = params.getOrDefault("external_reference", "");
            String status = params.getOrDefault("status", "");
            transactionService.handleWebhook(externalRef, mpPaymentId, status);
        }

        return ResponseEntity.ok().build();
    }
}
