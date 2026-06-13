package com.api.Store.dto;

import com.api.Store.enums.StoreRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class StoreUserResponseDTO {
    private UUID userId;
    private UUID storeId;
    private StoreRole role;
    @JsonProperty("isActive")
    private boolean isActive;
    private String userName;
    private String userEmail;
    private LocalDateTime createAt;
}
