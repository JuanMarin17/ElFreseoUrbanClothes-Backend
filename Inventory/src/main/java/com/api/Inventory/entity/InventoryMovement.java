package com.api.Inventory.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.api.Inventory.enums.MovementType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "inventory_movement")
public class InventoryMovement {

    @Id
    @GeneratedValue
    @Column(name = "movement_id")
    private UUID movementId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "store_id")
    private UUID storeId;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type")
    private MovementType movementType;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}