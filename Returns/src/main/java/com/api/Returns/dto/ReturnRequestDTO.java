package com.api.Returns.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class ReturnRequestDTO {
    private UUID orderId;
    private String reason;
    private List<ReturnItemRequestDTO> items;
}