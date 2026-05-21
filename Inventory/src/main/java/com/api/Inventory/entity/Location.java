package com.api.Inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "location")
public class Location {

    @Id
    @GeneratedValue
    @Column(name = "location_id")
    private UUID locationId;

    private String name;

    @Column(name = "store_id")
    private UUID storeId;
}