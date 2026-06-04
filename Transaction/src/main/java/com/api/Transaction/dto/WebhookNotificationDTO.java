package com.api.Transaction.dto;

import lombok.Data;

@Data
public class WebhookNotificationDTO {
    private String type;
    private WebhookData data;

    @Data
    public static class WebhookData {
        private String id;
    }
}
