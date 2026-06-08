package com.api.reports.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalOrderDTO {
    private UUID id;
    private UUID userId;
    private UUID storeId;
    private String orderNumber;
    private String status;
    private List<ExternalOrderItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal total;
    private ExternalPaymentDTO payment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
