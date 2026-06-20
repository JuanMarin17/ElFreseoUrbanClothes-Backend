package com.api.product.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.api.product.service.StockAlertService;

import lombok.RequiredArgsConstructor;

// El CORS se maneja centralizado en el Gateway (CorsWebFilter). Agregarlo también
// aquí duplica el header Access-Control-Allow-Origin y el navegador rechaza la
// respuesta con net::ERR_FAILED aunque el status real sea 200.
@RestController
@RequiredArgsConstructor
public class StockAlertController {

    private final StockAlertService stockAlertService;

    /**
     * Abre un stream SSE de alertas de stock para una tienda específica.
     * GET /alerts/stock/stream/{storeId}
     */
    @GetMapping("/alerts/stock/stream/{storeId}")
    public SseEmitter openStockAlertStream(@PathVariable UUID storeId) {
        return stockAlertService.subscribe(storeId);
    }
}
