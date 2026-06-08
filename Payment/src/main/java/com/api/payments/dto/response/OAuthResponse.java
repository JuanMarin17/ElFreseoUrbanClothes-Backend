package com.api.payments.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuthResponse {
    private String tenantId;
    private String mpUserId;
    private boolean connected;
    private String message;
    /** URL para iniciar el flujo OAuth (se devuelve al frontend) */
    private String authorizationUrl;
}
