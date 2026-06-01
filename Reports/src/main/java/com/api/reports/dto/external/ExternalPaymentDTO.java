package com.api.reports.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalPaymentDTO {
    private UUID id;
    private BigDecimal amount;
    private String status;
    private String method;
    private LocalDateTime paidAt;
}
