package com.api.product.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private UUID categoryId;
    @Column(name = "store_id")
    private UUID storeId;
    @Column(name = "name")
    private String name;
    @Column(name = "active")
    private Boolean active;

    // ── Atributos de variante configurables (ej. "Capacidad" para Tecnología,
    //    "Material" para Hogar, en vez de los Talla/Color por defecto) ───────
    @Column(name = "attribute1_label")
    private String attribute1Label;
    @Column(name = "attribute1_options_json", columnDefinition = "TEXT")
    private String attribute1OptionsJson;
    @Column(name = "attribute2_label")
    private String attribute2Label;
    @Column(name = "attribute2_options_json", columnDefinition = "TEXT")
    private String attribute2OptionsJson;
    @Column(name = "attribute2_is_color")
    private Boolean attribute2IsColor;

    @PrePersist
    public void prePersist() {
        this.active = true;
    }
}