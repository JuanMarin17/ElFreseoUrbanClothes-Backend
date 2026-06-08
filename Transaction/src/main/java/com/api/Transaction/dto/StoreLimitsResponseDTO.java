package com.api.Transaction.dto;

import com.api.Transaction.enums.PlanName;

import lombok.Data;

@Data
public class StoreLimitsResponseDTO {
    private PlanName planName;
    private boolean active;
    private Integer maxProducts;
    private Integer maxPages;
    private Integer maxAiCalls;
    private String features;
}