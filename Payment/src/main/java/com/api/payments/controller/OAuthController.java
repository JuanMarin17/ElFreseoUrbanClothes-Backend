package com.api.payments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.api.payments.config.MercadoPagoConfiguration;
import com.api.payments.dto.response.OAuthResponse;
import com.api.payments.service.OAuthService;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final OAuthService oauthService;
    private final MercadoPagoConfiguration mpConfig;

    /**
     * GET /api/oauth/connect?tenantId=xxx
     * El tenant llama a este endpoint para iniciar la conexión de su cuenta MP.
     * Devuelve la URL de autorización donde el tenant debe redirigirse.
     */
    @GetMapping("/connect")
    public ResponseEntity<OAuthResponse> connect(@RequestParam String tenantId) {
        return ResponseEntity.ok(oauthService.getAuthorizationUrl(tenantId));
    }

    /**
     * GET /api/oauth/callback?code=XXX&state=tenantId
     * Mercado Pago redirige aquí después de que el tenant autoriza.
     * Intercambiamos el code por access_token y redirigimos al frontend.
     */
    @GetMapping("/callback")
    public RedirectView callback(
        @RequestParam String code,
        @RequestParam(name = "state") String tenantId,
        @RequestParam(required = false) String error
    ) {
        String frontendBase = mpConfig.getFrontendUrl() + "/dashboard/settings/payments";

        if (error != null) {
            log.warn("OAuth rechazado por tenant={}: {}", tenantId, error);
            return new RedirectView(frontendBase + "?status=error&reason=" + error);
        }

        try {
            oauthService.handleCallback(code, tenantId);
            return new RedirectView(frontendBase + "?status=connected");
        } catch (Exception e) {
            log.error("Error en callback OAuth para tenant={}", tenantId, e);
            return new RedirectView(frontendBase + "?status=error&reason=internal");
        }
    }

    /**
     * DELETE /api/oauth/disconnect/{tenantId}
     * Desconecta la cuenta MP de un tenant.
     */
    @DeleteMapping("/disconnect/{tenantId}")
    public ResponseEntity<OAuthResponse> disconnect(@PathVariable String tenantId) {
        oauthService.disconnectTenant(tenantId);
        return ResponseEntity.ok(OAuthResponse.builder()
            .tenantId(tenantId)
            .connected(false)
            .message("Cuenta Mercado Pago desconectada")
            .build());
    }
}
