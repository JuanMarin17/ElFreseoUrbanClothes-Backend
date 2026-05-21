package com.api.product.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "image_id")
    private UUID imageId;

    // TEXT en PostgreSQL no tiene límite de caracteres.
    // Soporta tanto URLs de Cloudinary (cortas) como base64 (largas).
    @Column(name = "url", columnDefinition = "TEXT", nullable = false)
    private String url;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}