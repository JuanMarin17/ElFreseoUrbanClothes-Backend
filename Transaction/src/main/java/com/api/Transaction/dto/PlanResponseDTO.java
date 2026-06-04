package com.api.Transaction.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.api.Transaction.enums.PlanName;

import lombok.Data;

@Data
public class PlanResponseDTO {
    private UUID planId;
    private PlanName name;
    private BigDecimal price;
    private Integer maxProducts;
    private Integer maxPages;
    private Integer maxAiCalls;
    private String features;
}