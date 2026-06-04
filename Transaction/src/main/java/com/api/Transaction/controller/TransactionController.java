package com.api.Transaction.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.Transaction.dto.CheckoutRequestDTO;
import com.api.Transaction.dto.CheckoutResponseDTO;
import com.api.Transaction.dto.PlanChangeHistoryResponseDTO;
import com.api.Transaction.dto.StoreLimitsResponseDTO;
import com.api.Transaction.dto.SubscriptionResponseDTO;
import com.api.Transaction.dto.TransactionResponseDTO;
import com.api.Transaction.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * OWNER inicia el pago de un plan.
     * POST /api/v1/transactions/checkout
     * Devuelve la URL de pago de MercadoPago (null si es plan GRATUITO).
     */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDTO> checkout(@Valid @RequestBody CheckoutRequestDTO dto) {
        return ResponseEntity.ok(transactionService.checkout(dto));
    }

    /**
     * Suscripción activa de una tienda.
     * GET /api/v1/transactions/subscription/{storeId}
     */
    @GetMapping("/subscription/{storeId}")
    public ResponseEntity<SubscriptionResponseDTO> getSubscription(@PathVariable UUID storeId) {
        return ResponseEntity.ok(transactionService.getActiveSubscription(storeId));
    }

    /**
     * Límites del plan activo — usado por otros microservicios para validar.
     * GET /api/v1/transactions/limits/{storeId}
     */
    @GetMapping("/limits/{storeId}")
    public ResponseEntity<StoreLimitsResponseDTO> getLimits(@PathVariable UUID storeId) {
        return ResponseEntity.ok(transactionService.getStoreLimits(storeId));
    }

    /**
     * Historial de transacciones de una tienda (OWNER).
     * GET /api/v1/transactions/history/{storeId}
     */
    @GetMapping("/history/{storeId}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions(@PathVariable UUID storeId) {
        return ResponseEntity.ok(transactionService.getTransactionsByStore(storeId));
    }

    /**
     * Historial de cambios de plan (OWNER).
     * GET /api/v1/transactions/plan-history/{storeId}
     */
    @GetMapping("/plan-history/{storeId}")
    public ResponseEntity<List<PlanChangeHistoryResponseDTO>> getPlanHistory(@PathVariable UUID storeId) {
        return ResponseEntity.ok(transactionService.getPlanHistory(storeId));
    }

    /**
     * Todas las transacciones del sistema (solo SUPERADMIN).
     * GET /api/v1/transactions/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    /**
     * Cancelar suscripción activa (OWNER).
     * DELETE /api/v1/transactions/subscription/{storeId}
     */
    @DeleteMapping("/subscription/{storeId}")
    public ResponseEntity<Void> cancelSubscription(@PathVariable UUID storeId) {
        transactionService.cancelSubscription(storeId);
        return ResponseEntity.noContent().build();
    }
}
