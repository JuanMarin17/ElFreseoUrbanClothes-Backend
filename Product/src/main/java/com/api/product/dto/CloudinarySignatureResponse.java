package com.api.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CloudinarySignatureResponse {
    private String signature;
    private long timestamp;
    private String apiKey;
    private String cloudName;
    private String folder;
}