package com.api.reports.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalProductVariantDTO {
    private UUID variantId;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private Integer minStock;
}
