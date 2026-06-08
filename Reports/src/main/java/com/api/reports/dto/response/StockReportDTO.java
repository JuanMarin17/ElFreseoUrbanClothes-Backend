package com.api.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class StockReportDTO {

    @Data
    @Builder
    public static class StockSummary {
        private int totalProducts;
        private int activeProducts;
        private int inactiveProducts;
        private int totalVariants;
        private int lowStockVariants;
        private int outOfStockVariants;
        private int healthyStockVariants;
        private BigDecimal totalStockValue;
    }

    @Data
    @Builder
    public static class CategoryStockDTO {
        private String categoryName;
        private int productCount;
        private int totalStock;
        private int lowStockCount;
        private int outOfStockCount;
    }

    private StockSummary summary;
    private List<LowStockItemDTO> lowStockItems;
    private List<LowStockItemDTO> outOfStockItems;
    private List<CategoryStockDTO> byCategory;
}
