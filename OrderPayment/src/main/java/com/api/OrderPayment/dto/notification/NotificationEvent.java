package com.api.OrderPayment.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationEvent {
    private String type;
    private String title;
    private String message;
    private Object data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
