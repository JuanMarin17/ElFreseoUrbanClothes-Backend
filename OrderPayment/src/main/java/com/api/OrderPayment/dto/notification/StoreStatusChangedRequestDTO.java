package com.api.OrderPayment.dto.notification;

import lombok.Data;

import java.util.UUID;

@Data
public class StoreStatusChangedRequestDTO {
    private UUID storeId;
    private String storeName;
    private Boolean isActive;
    private String reason;
}
