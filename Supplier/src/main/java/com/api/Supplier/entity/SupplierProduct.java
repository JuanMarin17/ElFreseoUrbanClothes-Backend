package com.api.Supplier.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "supplier_product")
public class SupplierProduct {

    @EmbeddedId
    private SupplierProductId id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Data
    @Embeddable
    public static class SupplierProductId implements Serializable {
        @Column(name = "supplier_id")
        private UUID supplierId;

        @Column(name = "product_id")
        private UUID productId;
    }
}
