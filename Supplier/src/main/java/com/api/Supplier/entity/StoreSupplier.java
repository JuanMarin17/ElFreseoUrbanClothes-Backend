package com.api.Supplier.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "store_supplier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(StoreSupplier.StoreSupplierKey.class)
public class StoreSupplier {

    @Id
    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Id
    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", insertable = false, updatable = false)
    private Supplier supplier;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StoreSupplierKey implements Serializable {
        private UUID storeId;
        private UUID supplierId;
    }
}