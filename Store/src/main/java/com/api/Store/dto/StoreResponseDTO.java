package com.api.Store.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StoreResponseDTO {
    private UUID storeId;
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;
    private String message;
    private Integer status;
}
