package com.api.OrderPayment.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class NotificationResponseDTO {
    private UUID id;
    private String type;
    private String title;
    private String message;
    private Map<String, Object> data;
    private Boolean read;
    private LocalDateTime createdAt;
}
