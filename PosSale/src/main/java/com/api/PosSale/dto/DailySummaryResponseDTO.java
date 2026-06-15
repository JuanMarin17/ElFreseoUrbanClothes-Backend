package com.api.PosSale.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DailySummaryResponseDTO {

    private LocalDate date;
    private long totalSales;
    private long cancelledSales;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscount;
    private BigDecimal averageSale;
}
