package com.api.reports.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalProductDTO {
    private UUID productId;
    private String name;
    private String status;
    private String brandName;
    private List<ExternalProductVariantDTO> variants;
    private List<String> categories;
}
