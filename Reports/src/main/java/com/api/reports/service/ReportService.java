package com.api.reports.service;

import com.api.reports.client.OrderClient;
import com.api.reports.client.ProductClient;
import com.api.reports.dto.external.ExternalOrderDTO;
import com.api.reports.dto.external.ExternalOrderItemDTO;
import com.api.reports.dto.external.ExternalProductDTO;
import com.api.reports.dto.external.ExternalProductVariantDTO;
import com.api.reports.dto.response.*;
import com.common_request_context_starter.context.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ProductClient productClient;
    private final OrderClient orderClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Set<String> ACTIVE_ORDER_STATUSES = Set.of(
            "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED"
    );
    private static final Set<String> CANCELLED_STATUSES = Set.of("CANCELLED", "REFUNDED");

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getStoreId() {
        String storeId = RequestContext.getHeader("x-store-id");
        if (storeId == null || storeId.isBlank())
            throw new IllegalArgumentException("X-Store-Id es requerido");
        return storeId;
    }

    private boolean isRevenueCounted(ExternalOrderDTO order) {
        return order.getStatus() != null && !CANCELLED_STATUSES.contains(order.getStatus())
                && order.getPayment() != null
                && "APPROVED".equals(order.getPayment().getStatus());
    }

    private BigDecimal sumRevenue(List<ExternalOrderDTO> orders) {
        return orders.stream()
                .filter(this::isRevenueCounted)
                .map(o -> o.getPayment().getAmount() != null ? o.getPayment().getAmount() : o.getTotal())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ExternalOrderDTO> filterByPeriod(List<ExternalOrderDTO> orders, LocalDateTime from) {
        return orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(from))
                .collect(Collectors.toList());
    }

    private List<TopProductDTO> buildTopProducts(List<ExternalOrderDTO> orders, int limit) {
        Map<String, long[]> aggregated = new LinkedHashMap<>();

        for (ExternalOrderDTO order : orders) {
            if (order.getItems() == null) continue;
            for (ExternalOrderItemDTO item : order.getItems()) {
                String key = item.getProductName();
                aggregated.computeIfAbsent(key, k -> new long[]{0, 0});
                aggregated.get(key)[0] += item.getQuantity() != null ? item.getQuantity() : 0;
                if (item.getSubtotal() != null)
                    aggregated.get(key)[1] += item.getSubtotal().longValue();
            }
        }

        return aggregated.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(limit)
                .map(e -> TopProductDTO.builder()
                        .productName(e.getKey())
                        .unitsSold(e.getValue()[0])
                        .revenue(BigDecimal.valueOf(e.getValue()[1]))
                        .build())
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. DASHBOARD
    // ═══════════════════════════════════════════════════════════════════════════

    public DashboardSummaryDTO getDashboard() {
        String storeId = getStoreId();

        List<ExternalProductDTO> products = productClient.getAllProducts(storeId);
        List<ExternalOrderDTO> orders = orderClient.getAllOrders(UUID.fromString(storeId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();

        // Stock
        int totalProducts = products.size();
        int activeProducts = (int) products.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count();
        int inactiveProducts = totalProducts - activeProducts;
        int totalVariants = 0, lowStock = 0, outOfStock = 0;
        BigDecimal stockValue = BigDecimal.ZERO;

        for (ExternalProductDTO p : products) {
            if (p.getVariants() == null) continue;
            for (ExternalProductVariantDTO v : p.getVariants()) {
                totalVariants++;
                int stock = v.getStock() != null ? v.getStock() : 0;
                int minStock = v.getMinStock() != null ? v.getMinStock() : 0;
                if (stock == 0) outOfStock++;
                else if (stock <= minStock) lowStock++;
                if (v.getPrice() != null)
                    stockValue = stockValue.add(BigDecimal.valueOf(stock).multiply(v.getPrice()));
            }
        }

        // Órdenes
        int totalOrders = orders.size();
        Map<String, Long> byStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus() != null ? o.getStatus() : "UNKNOWN",
                        Collectors.counting()));

        // Ingresos
        BigDecimal totalRevenue = sumRevenue(orders);
        BigDecimal todayRevenue = sumRevenue(filterByPeriod(orders, startOfToday));
        BigDecimal weekRevenue = sumRevenue(filterByPeriod(orders, now.minusDays(7)));
        BigDecimal monthRevenue = sumRevenue(filterByPeriod(orders, now.minusDays(30)));

        return DashboardSummaryDTO.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .inactiveProducts(inactiveProducts)
                .totalVariants(totalVariants)
                .lowStockVariants(lowStock)
                .outOfStockVariants(outOfStock)
                .stockValue(stockValue.setScale(2, RoundingMode.HALF_UP))
                .totalOrders(totalOrders)
                .pendingOrders(byStatus.getOrDefault("PENDING", 0L).intValue())
                .confirmedOrders(byStatus.getOrDefault("CONFIRMED", 0L).intValue())
                .processingOrders(byStatus.getOrDefault("PROCESSING", 0L).intValue())
                .shippedOrders(byStatus.getOrDefault("SHIPPED", 0L).intValue())
                .deliveredOrders(byStatus.getOrDefault("DELIVERED", 0L).intValue())
                .cancelledOrders(byStatus.getOrDefault("CANCELLED", 0L).intValue())
                .refundedOrders(byStatus.getOrDefault("REFUNDED", 0L).intValue())
                .totalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                .todayRevenue(todayRevenue.setScale(2, RoundingMode.HALF_UP))
                .weekRevenue(weekRevenue.setScale(2, RoundingMode.HALF_UP))
                .monthRevenue(monthRevenue.setScale(2, RoundingMode.HALF_UP))
                .topProducts(buildTopProducts(orders, 5))
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. REPORTE DE STOCK
    // ═══════════════════════════════════════════════════════════════════════════

    public StockReportDTO getStockReport() {
        String storeId = getStoreId();
        List<ExternalProductDTO> products = productClient.getAllProducts(storeId);

        int totalProducts = products.size();
        int activeProducts = (int) products.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count();
        int totalVariants = 0, lowStock = 0, outOfStock = 0, healthy = 0;
        BigDecimal stockValue = BigDecimal.ZERO;

        List<LowStockItemDTO> lowStockItems = new ArrayList<>();
        List<LowStockItemDTO> outOfStockItems = new ArrayList<>();
        Map<String, int[]> categoryStats = new LinkedHashMap<>();

        for (ExternalProductDTO p : products) {
            if (p.getVariants() == null) continue;
            List<String> cats = p.getCategories() != null ? p.getCategories() : List.of("Sin categoría");

            for (ExternalProductVariantDTO v : p.getVariants()) {
                totalVariants++;
                int stock = v.getStock() != null ? v.getStock() : 0;
                int minStock = v.getMinStock() != null ? v.getMinStock() : 0;
                BigDecimal price = v.getPrice() != null ? v.getPrice() : BigDecimal.ZERO;

                if (v.getPrice() != null)
                    stockValue = stockValue.add(BigDecimal.valueOf(stock).multiply(price));

                LowStockItemDTO item = LowStockItemDTO.builder()
                        .productId(p.getProductId())
                        .productName(p.getName())
                        .sku(v.getSku())
                        .currentStock(stock)
                        .minStock(minStock)
                        .price(price)
                        .build();

                if (stock == 0) {
                    outOfStock++;
                    outOfStockItems.add(item);
                } else if (stock <= minStock) {
                    lowStock++;
                    lowStockItems.add(item);
                } else {
                    healthy++;
                }

                // Stats por categoría
                for (String cat : cats) {
                    int[] stats = categoryStats.computeIfAbsent(cat, k -> new int[4]);
                    stats[0]++;           // productCount
                    stats[1] += stock;    // totalStock
                    if (stock == 0) stats[3]++;
                    else if (stock <= minStock) stats[2]++;
                }
            }
        }

        List<StockReportDTO.CategoryStockDTO> byCategory = categoryStats.entrySet().stream()
                .map(e -> StockReportDTO.CategoryStockDTO.builder()
                        .categoryName(e.getKey())
                        .productCount(e.getValue()[0])
                        .totalStock(e.getValue()[1])
                        .lowStockCount(e.getValue()[2])
                        .outOfStockCount(e.getValue()[3])
                        .build())
                .sorted(Comparator.comparingInt(StockReportDTO.CategoryStockDTO::getOutOfStockCount).reversed())
                .collect(Collectors.toList());

        return StockReportDTO.builder()
                .summary(StockReportDTO.StockSummary.builder()
                        .totalProducts(totalProducts)
                        .activeProducts(activeProducts)
                        .inactiveProducts(totalProducts - activeProducts)
                        .totalVariants(totalVariants)
                        .lowStockVariants(lowStock)
                        .outOfStockVariants(outOfStock)
                        .healthyStockVariants(healthy)
                        .totalStockValue(stockValue.setScale(2, RoundingMode.HALF_UP))
                        .build())
                .lowStockItems(lowStockItems)
                .outOfStockItems(outOfStockItems)
                .byCategory(byCategory)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. REPORTE DE ÓRDENES
    // ═══════════════════════════════════════════════════════════════════════════

    public OrdersReportDTO getOrdersReport(int days) {
        String storeId = getStoreId();
        List<ExternalOrderDTO> allOrders = orderClient.getAllOrders(UUID.fromString(storeId));
        List<ExternalOrderDTO> orders = days > 0
                ? filterByPeriod(allOrders, LocalDateTime.now().minusDays(days))
                : allOrders;

        Map<String, Long> byStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus() != null ? o.getStatus() : "UNKNOWN",
                        Collectors.counting()));

        BigDecimal totalRevenue = sumRevenue(orders);
        long paidOrders = orders.stream().filter(this::isRevenueCounted).count();
        BigDecimal avgTicket = paidOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Breakdown diario
        Map<String, long[]> dailyMap = new TreeMap<>();
        for (ExternalOrderDTO o : orders) {
            if (o.getCreatedAt() == null) continue;
            String day = o.getCreatedAt().format(DATE_FORMATTER);
            dailyMap.computeIfAbsent(day, k -> new long[]{0, 0});
            dailyMap.get(day)[0]++;
            if (isRevenueCounted(o) && o.getPayment() != null && o.getPayment().getAmount() != null)
                dailyMap.get(day)[1] += o.getPayment().getAmount().longValue();
        }

        List<OrdersReportDTO.DailyOrderDTO> daily = dailyMap.entrySet().stream()
                .map(e -> OrdersReportDTO.DailyOrderDTO.builder()
                        .date(e.getKey())
                        .orderCount((int) e.getValue()[0])
                        .revenue(BigDecimal.valueOf(e.getValue()[1]))
                        .build())
                .collect(Collectors.toList());

        return OrdersReportDTO.builder()
                .summary(OrdersReportDTO.OrderSummary.builder()
                        .total(orders.size())
                        .pending(byStatus.getOrDefault("PENDING", 0L).intValue())
                        .confirmed(byStatus.getOrDefault("CONFIRMED", 0L).intValue())
                        .processing(byStatus.getOrDefault("PROCESSING", 0L).intValue())
                        .shipped(byStatus.getOrDefault("SHIPPED", 0L).intValue())
                        .delivered(byStatus.getOrDefault("DELIVERED", 0L).intValue())
                        .cancelled(byStatus.getOrDefault("CANCELLED", 0L).intValue())
                        .refunded(byStatus.getOrDefault("REFUNDED", 0L).intValue())
                        .totalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                        .averageOrderValue(avgTicket)
                        .build())
                .dailyBreakdown(daily)
                .topProducts(buildTopProducts(orders, 10))
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 4. REPORTE DE VENTAS
    // ═══════════════════════════════════════════════════════════════════════════

    public SalesReportDTO getSalesReport(int days) {
        String storeId = getStoreId();
        List<ExternalOrderDTO> allOrders = orderClient.getAllOrders(UUID.fromString(storeId));

        LocalDateTime now = LocalDateTime.now();
        List<ExternalOrderDTO> orders = days > 0
                ? filterByPeriod(allOrders, now.minusDays(days))
                : allOrders;

        List<ExternalOrderDTO> paidOrders = orders.stream()
                .filter(this::isRevenueCounted)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = sumRevenue(orders);
        BigDecimal todayRevenue = sumRevenue(filterByPeriod(orders, now.toLocalDate().atStartOfDay()));
        BigDecimal weekRevenue = sumRevenue(filterByPeriod(orders, now.minusDays(7)));
        BigDecimal monthRevenue = sumRevenue(filterByPeriod(orders, now.minusDays(30)));
        BigDecimal avgTicket = !paidOrders.isEmpty()
                ? totalRevenue.divide(BigDecimal.valueOf(paidOrders.size()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Breakdown diario
        Map<String, BigDecimal[]> dailyRevMap = new TreeMap<>();
        for (ExternalOrderDTO o : paidOrders) {
            if (o.getCreatedAt() == null || o.getPayment() == null) continue;
            String day = o.getCreatedAt().format(DATE_FORMATTER);
            BigDecimal amt = o.getPayment().getAmount() != null ? o.getPayment().getAmount() : o.getTotal();
            if (amt == null) continue;
            dailyRevMap.computeIfAbsent(day, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            dailyRevMap.get(day)[0] = dailyRevMap.get(day)[0].add(amt);
            dailyRevMap.get(day)[1] = dailyRevMap.get(day)[1].add(BigDecimal.ONE);
        }

        List<SalesReportDTO.DailySaleDTO> daily = dailyRevMap.entrySet().stream()
                .map(e -> SalesReportDTO.DailySaleDTO.builder()
                        .date(e.getKey())
                        .revenue(e.getValue()[0].setScale(2, RoundingMode.HALF_UP))
                        .orderCount(e.getValue()[1].intValue())
                        .build())
                .collect(Collectors.toList());

        // Por método de pago
        Map<String, List<ExternalOrderDTO>> byMethod = paidOrders.stream()
                .filter(o -> o.getPayment() != null && o.getPayment().getMethod() != null)
                .collect(Collectors.groupingBy(o -> o.getPayment().getMethod()));

        BigDecimal finalTotal = totalRevenue;
        List<SalesReportDTO.PaymentMethodDTO> byPayment = byMethod.entrySet().stream()
                .map(e -> {
                    BigDecimal methodTotal = sumRevenue(e.getValue());
                    double pct = finalTotal.compareTo(BigDecimal.ZERO) > 0
                            ? methodTotal.divide(finalTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100
                            : 0;
                    return SalesReportDTO.PaymentMethodDTO.builder()
                            .method(e.getKey())
                            .count(e.getValue().size())
                            .total(methodTotal.setScale(2, RoundingMode.HALF_UP))
                            .percentage(Math.round(pct * 10.0) / 10.0)
                            .build();
                })
                .sorted(Comparator.comparingDouble(SalesReportDTO.PaymentMethodDTO::getTotal)
                        .reversed())
                .collect(Collectors.toList());

        return SalesReportDTO.builder()
                .summary(SalesReportDTO.SalesSummary.builder()
                        .totalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP))
                        .todayRevenue(todayRevenue.setScale(2, RoundingMode.HALF_UP))
                        .weekRevenue(weekRevenue.setScale(2, RoundingMode.HALF_UP))
                        .monthRevenue(monthRevenue.setScale(2, RoundingMode.HALF_UP))
                        .totalOrdersPaid(paidOrders.size())
                        .averageTicket(avgTicket)
                        .build())
                .dailyBreakdown(daily)
                .byPaymentMethod(byPayment)
                .topProductsByRevenue(buildTopProducts(paidOrders, 10))
                .build();
    }
}
