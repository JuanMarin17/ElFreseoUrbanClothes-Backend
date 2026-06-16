package com.api.OrderPayment.dto.notification;

import lombok.Data;

@Data
public class SessionAlertRequestDTO {
    private String ip;
    private String userAgent;
}
