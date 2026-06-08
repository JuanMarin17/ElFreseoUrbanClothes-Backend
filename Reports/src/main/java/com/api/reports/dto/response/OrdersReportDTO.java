package com.api.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class OrdersReportDTO {

    @Data
    @Builder
    public static class OrderSummary {
        private int total;
        private int pending;
        private int confirmed;
        private int processing;
        private int shipped;
        private int delivered;
        private int cancelled;
        private int refunded;
        private BigDecimal totalRevenue;
        private BigDecimal averageOrderValue;
    }

    @Data
    @Builder
    public static class DailyOrderDTO {
        private String date;
        private int orderCount;
        private BigDecimal revenue;
    }

    private OrderSummary summary;
    private List<DailyOrderDTO> dailyBreakdown;
    private List<TopProductDTO> topProducts;
}
