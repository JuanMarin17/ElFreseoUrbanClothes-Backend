package com.api.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardSummaryDTO {

    // ── Productos ─────────────────────────────────────────────────────────────
    private int totalProducts;
    private int activeProducts;
    private int inactiveProducts;
    private int totalVariants;
    private int lowStockVariants;
    private int outOfStockVariants;
    private BigDecimal stockValue;

    // ── Órdenes ───────────────────────────────────────────────────────────────
    private int totalOrders;
    private int pendingOrders;
    private int confirmedOrders;
    private int processingOrders;
    private int shippedOrders;
    private int deliveredOrders;
    private int cancelledOrders;
    private int refundedOrders;

    // ── Ingresos ──────────────────────────────────────────────────────────────
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;

    // ── Top productos ─────────────────────────────────────────────────────────
    private List<TopProductDTO> topProducts;

    // ── Ingresos por día (últimos 30 días) ────────────────────────────────────
    private List<DailyRevenueDTO> revenueByDay;

    @Data
    @Builder
    public static class DailyRevenueDTO {
        private String date;
        private BigDecimal amount;
    }
}
