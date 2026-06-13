package com.api.Store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponseDTO {
    private UUID storeId;
    private UUID ownerId;
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;
    private String message;
    private Integer status;
}
