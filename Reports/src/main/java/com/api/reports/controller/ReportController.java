package com.api.reports.controller;

import com.api.reports.dto.response.DashboardSummaryDTO;
import com.api.reports.dto.response.OrdersReportDTO;
import com.api.reports.dto.response.SalesReportDTO;
import com.api.reports.dto.response.StockReportDTO;
import com.api.reports.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/v1/reports/dashboard
     * Resumen general: productos, stock, órdenes e ingresos de la tienda.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryDTO> dashboard() {
        return ResponseEntity.ok(reportService.getDashboard());
    }

    /**
     * GET /api/v1/reports/stock
     * Estado del inventario: stock bajo, agotado y por categoría.
     */
    @GetMapping("/stock")
    public ResponseEntity<StockReportDTO> stock() {
        return ResponseEntity.ok(reportService.getStockReport());
    }

    /**
     * GET /api/v1/reports/orders?days=30
     * Estadísticas de órdenes. days=0 devuelve todo el histórico.
     */
    @GetMapping("/orders")
    public ResponseEntity<OrdersReportDTO> orders(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reportService.getOrdersReport(days));
    }

    /**
     * GET /api/v1/reports/sales?days=30
     * Ingresos por período, por método de pago y top productos por revenue.
     * days=0 devuelve todo el histórico.
     */
    @GetMapping("/sales")
    public ResponseEntity<SalesReportDTO> sales(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reportService.getSalesReport(days));
    }
}
