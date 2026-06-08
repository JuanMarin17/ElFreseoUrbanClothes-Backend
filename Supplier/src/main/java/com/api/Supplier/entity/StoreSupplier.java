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
@Table(name = "store_supplier")
public class StoreSupplier {

    @EmbeddedId
    private StoreSuppliedId id;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Data
    @Embeddable
    public static class StoreSuppliedId implements Serializable {
        @Column(name = "store_id")
        private UUID storeId;

        @Column(name = "supplier_id")
        private UUID supplierId;
    }
}