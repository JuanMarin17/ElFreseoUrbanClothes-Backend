package com.api.Transaction.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyRevenueDTO {
    private String month;
    private double basico;
    private double pro;
    private double premium;
}
