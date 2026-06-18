package com.api.OrderPayment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Notificación persistida para un usuario (bandeja de notificaciones).
 * Se guarda independientemente de si el usuario tiene una conexión SSE activa,
 * para que pueda verla más tarde aunque no estuviera conectado cuando ocurrió.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;

    @Column(nullable = false)
    @Builder.Default
    private Boolean read = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
