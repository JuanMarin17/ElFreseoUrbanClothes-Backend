package com.api.Store.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "store_settings")
public class StoreSettings {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "completed_step")
    private Integer completedStep;

    @Column(name = "logo_url")
    private String logoUrl;

    // Objeto plan: { id, name, price, features }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "plan", columnDefinition = "jsonb")
    private Map<String, Object> plan;

    // Datos básicos de la tienda: { name, description }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "basic", columnDefinition = "jsonb")
    private Map<String, Object> basic;

    // Componentes visuales: { banner, header, footer }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "components", columnDefinition = "jsonb")
    private Map<String, Object> components;

    // Layout elegido: { id, title, description, img }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout", columnDefinition = "jsonb")
    private Map<String, Object> layout;

    // Datos legales: { legalName, idNumber, documentName }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "legal", columnDefinition = "jsonb")
    private Map<String, Object> legal;

    // Método de pago y envío: { paymentMethod, shipping }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment", columnDefinition = "jsonb")
    private Map<String, Object> payment;

    // Configuración de preview del builder
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preview", columnDefinition = "jsonb")
    private Map<String, Object> preview;

    // Estilos visuales (colores, fuentes, bordes, etc.)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "styles", columnDefinition = "jsonb")
    private Map<String, Object> styles;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
