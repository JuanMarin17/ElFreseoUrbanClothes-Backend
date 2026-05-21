package com.api.Inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "inventory_balance")
public class InventoryBalance {

    @Id
    @GeneratedValue
    @Column(name = "balance_id")
    private UUID balanceId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "store_id")
    private UUID storeId;

    private Integer quantity = 0;
}