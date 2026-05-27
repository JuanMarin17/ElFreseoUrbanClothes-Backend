package com.tuapp.payments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Credenciales OAuth de la cuenta Mercado Pago del tenant.
 * Necesarias para el Marketplace (split de pagos).
 * El tenant conecta SU cuenta de MP y nosotros guardamos el access_token.
 */
@Entity
@Table(name = "tenant_mp_credentials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TenantMpCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private String tenantId;

    /** Access token de la cuenta MP del tenant (cifrado en prod) */
    @Column(name = "access_token", nullable = false, length = 512)
    private String accessToken;

    /** Refresh token para renovar el access_token */
    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    /** User ID del tenant en Mercado Pago */
    @Column(name = "mp_user_id")
    private String mpUserId;

    /** Cuándo expira el access_token */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isTokenExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now().plusMinutes(5));
    }
}
