package com.api.Returns.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.api.Returns.enums.ReturnStatus;

import lombok.Data;

@Data
public class ReturnResponseDTO {
    private UUID returnId;
    private UUID orderId;
    private UUID userId;
    private UUID storeId;
    private String reason;
    private ReturnStatus status;
    private OffsetDateTime createdAt;
    private List<ReturnItemResponseDTO> items;
}