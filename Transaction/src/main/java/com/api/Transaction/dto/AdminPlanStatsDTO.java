package com.api.Transaction.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminPlanStatsDTO {
    private String id;
    private String label;
    private BigDecimal price;
    private long stores;
    private double mrr;
    private double growthPercent;
    private double churnPercent;
    private long newThisMonth;
}
