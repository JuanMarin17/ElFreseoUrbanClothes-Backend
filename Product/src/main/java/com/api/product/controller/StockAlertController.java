package com.api.product.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.api.product.service.StockAlertService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
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
