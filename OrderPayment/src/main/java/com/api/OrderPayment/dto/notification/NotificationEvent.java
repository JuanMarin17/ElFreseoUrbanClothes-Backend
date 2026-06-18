package com.api.OrderPayment.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationEvent {
    private UUID id;
    private String type;
    private String title;
    private String message;
    private Object data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
