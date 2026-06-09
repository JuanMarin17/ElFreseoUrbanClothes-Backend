package com.api.PosSale.controller;

import com.api.PosSale.dto.CreatePosSaleRequestDTO;
import com.api.PosSale.dto.DailySummaryResponseDTO;
import com.api.PosSale.dto.PosSaleResponseDTO;
import com.api.PosSale.service.PosSaleService;
import com.api.PosSale.util.HeaderUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * POST   /stores/{storeId}/pos/sales                           → Registrar venta POS
 * GET    /stores/{storeId}/pos/sales                           → Listar todas las ventas
 * GET    /stores/{storeId}/pos/sales/{saleId}                  → Ver detalle de venta
 * PATCH  /stores/{storeId}/pos/sales/{saleId}/cancel          → Cancelar venta
 * GET    /stores/{storeId}/pos/sales/daily                     → Resumen del día
 * GET    /stores/{storeId}/pos/sales/by-date?from=&to=         → Filtrar por rango de fechas
 * GET    /stores/{storeId}/pos/sales/customer/{customerId}     → Ventas de un cliente
 */
@RestController
@RequestMapping("/stores/{storeId}/pos/sales")
@RequiredArgsConstructor
public class PosSaleController {

    private final PosSaleService saleService;
    private final HeaderUtil headerUtil;

    @PostMapping
    public ResponseEntity<PosSaleResponseDTO> createSale(
            @PathVariable UUID storeId,
            @Valid @RequestBody CreatePosSaleRequestDTO dto) {

        UUID employeeId = headerUtil.requireUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.createSale(storeId, employeeId, dto));
    }

    @GetMapping
    public ResponseEntity<List<PosSaleResponseDTO>> listSales(@PathVariable UUID storeId) {
        return ResponseEntity.ok(saleService.getSalesByStore(storeId));
    }

    @GetMapping("/daily")
    public ResponseEntity<DailySummaryResponseDTO> dailySummary(@PathVariable UUID storeId) {
        return ResponseEntity.ok(saleService.getDailySummary(storeId));
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<PosSaleResponseDTO>> byDateRange(
            @PathVariable UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(saleService.getSalesByDateRange(storeId, from, to));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PosSaleResponseDTO>> byCustomer(
            @PathVariable UUID storeId,
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(saleService.getSalesByCustomer(storeId, customerId));
    }

    @GetMapping("/{saleId}")
    public ResponseEntity<PosSaleResponseDTO> getSale(
            @PathVariable UUID storeId,
            @PathVariable UUID saleId) {
        return ResponseEntity.ok(saleService.getSale(storeId, saleId));
    }

    @PatchMapping("/{saleId}/cancel")
    public ResponseEntity<PosSaleResponseDTO> cancelSale(
            @PathVariable UUID storeId,
            @PathVariable UUID saleId) {
        return ResponseEntity.ok(saleService.cancelSale(storeId, saleId));
    }
}
