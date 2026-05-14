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

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "primary_color")
    private String primaryColor;

    @Column(name = "secondary_color")
    private String secondaryColor;

    @Column(name = "font")
    private String font;

    @Column(name = "theme")
    private String theme;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout", columnDefinition = "jsonb")
    private Map<String, Object> layout;

    @Column(name = "currency")
    private String currency = "COP";

    @Column(name = "language")
    private String language = "es";

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
