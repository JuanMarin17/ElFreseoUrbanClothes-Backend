package com.api.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TopProductDTO {
    private UUID productId;
    private String productName;
    private long unitsSold;
    private BigDecimal revenue;
}
