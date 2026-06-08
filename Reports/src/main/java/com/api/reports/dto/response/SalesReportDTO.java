package com.api.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SalesReportDTO {

    @Data
    @Builder
    public static class SalesSummary {
        private BigDecimal totalRevenue;
        private BigDecimal todayRevenue;
        private BigDecimal weekRevenue;
        private BigDecimal monthRevenue;
        private int totalOrdersPaid;
        private BigDecimal averageTicket;
    }

    @Data
    @Builder
    public static class DailySaleDTO {
        private String date;
        private BigDecimal revenue;
        private int orderCount;
    }

    @Data
    @Builder
    public static class PaymentMethodDTO {
        private String method;
        private int count;
        private BigDecimal total;
        private double percentage;
    }

    private SalesSummary summary;
    private List<DailySaleDTO> dailyBreakdown;
    private List<PaymentMethodDTO> byPaymentMethod;
    private List<TopProductDTO> topProductsByRevenue;
}
