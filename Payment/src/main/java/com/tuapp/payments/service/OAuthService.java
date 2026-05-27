package com.tuapp.payments.service;

import com.tuapp.payments.config.MercadoPagoConfiguration;
import com.tuapp.payments.dto.response.OAuthResponse;
import com.tuapp.payments.entity.TenantMpCredential;
import com.tuapp.payments.exception.PaymentException;
import com.tuapp.payments.repository.TenantMpCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final TenantMpCredentialRepository credentialRepo;
    private final MercadoPagoConfiguration mpConfig;
    private final WebClient.Builder webClientBuilder;

    /**
     * Paso 1: Devuelve la URL de autorización OAuth de Mercado Pago.
     * El frontend redirige al usuario a esta URL.
     * El parámetro 'state' lleva el tenantId para poder asociar la cuenta al regresar.
     */
    public OAuthResponse getAuthorizationUrl(String tenantId) {
        String url = mpConfig.getOAuthUrl() + "&state=" + tenantId;

        return OAuthResponse.builder()
            .tenantId(tenantId)
            .connected(false)
            .authorizationUrl(url)
            .message("Redirige al usuario a authorizationUrl para conectar su cuenta MP")
            .build();
    }

    /**
     * Paso 2: Callback OAuth. MP llama a /api/oauth/callback?code=XXX&state=tenantId.
     * Intercambiamos el code por access_token y lo guardamos.
     */
    @Transactional
    public OAuthResponse handleCallback(String code, String tenantId) {
        log.info("OAuth callback para tenant={}", tenantId);

        Map<String, Object> tokenResponse = exchangeCodeForToken(code);

        String accessToken  = (String) tokenResponse.get("access_token");
        String refreshToken = (String) tokenResponse.get("refresh_token");
        Object mpUserId     = tokenResponse.get("user_id");
        Number expiresIn    = (Number) tokenResponse.getOrDefault("expires_in", 15552000);

        TenantMpCredential credential = credentialRepo.findByTenantId(tenantId)
            .orElse(TenantMpCredential.builder().tenantId(tenantId).build());

        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);
        credential.setMpUserId(mpUserId != null ? mpUserId.toString() : null);
        credential.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn.longValue()));
        credential.setIsActive(true);
        credentialRepo.save(credential);

        log.info("Cuenta MP conectada para tenant={} mpUserId={}", tenantId, mpUserId);

        return OAuthResponse.builder()
            .tenantId(tenantId)
            .mpUserId(mpUserId != null ? mpUserId.toString() : null)
            .connected(true)
            .message("Cuenta Mercado Pago conectada exitosamente")
            .build();
    }

    /**
     * Obtiene el access_token vigente del tenant,
     * renovándolo automáticamente si está próximo a expirar.
     */
    @Transactional
    public String getValidAccessToken(String tenantId) {
        TenantMpCredential cred = credentialRepo.findByTenantId(tenantId)
            .orElseThrow(() -> new com.tuapp.payments.exception.TenantNotConnectedException(tenantId));

        if (!cred.getIsActive()) {
            throw new com.tuapp.payments.exception.TenantNotConnectedException(tenantId);
        }

        if (cred.isTokenExpired()) {
            log.info("Token expirado para tenant={}, renovando...", tenantId);
            refreshAccessToken(cred);
        }

        return cred.getAccessToken();
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> exchangeCodeForToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id",     mpConfig.getClientId());
        body.add("client_secret", mpConfig.getClientSecret());
        body.add("grant_type",    "authorization_code");
        body.add("code",          code);
        body.add("redirect_uri",  mpConfig.getRedirectUri());

        try {
            return webClientBuilder.build()
                .post()
                .uri("https://api.mercadopago.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        } catch (Exception e) {
            log.error("Error intercambiando código OAuth: {}", e.getMessage());
            throw new PaymentException("Error al obtener token de Mercado Pago: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void refreshAccessToken(TenantMpCredential cred) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id",     mpConfig.getClientId());
        body.add("client_secret", mpConfig.getClientSecret());
        body.add("grant_type",    "refresh_token");
        body.add("refresh_token", cred.getRefreshToken());

        try {
            Map<String, Object> response = webClientBuilder.build()
                .post()
                .uri("https://api.mercadopago.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            cred.setAccessToken((String) response.get("access_token"));
            cred.setRefreshToken((String) response.get("refresh_token"));
            Number expiresIn = (Number) response.getOrDefault("expires_in", 15552000);
            cred.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn.longValue()));
            credentialRepo.save(cred);

        } catch (Exception e) {
            log.error("Error renovando token OAuth del tenant={}", cred.getTenantId());
            throw new PaymentException("Error renovando token de Mercado Pago", e);
        }
    }

    /** Desconecta la cuenta MP de un tenant */
    @Transactional
    public void disconnectTenant(String tenantId) {
        credentialRepo.findByTenantId(tenantId).ifPresent(cred -> {
            cred.setIsActive(false);
            credentialRepo.save(cred);
            log.info("Cuenta MP desconectada para tenant={}", tenantId);
        });
    }
}
