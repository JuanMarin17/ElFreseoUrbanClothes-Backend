package com.api.Returns.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "return_item")
public class ReturnItem {

    @Id
    @GeneratedValue
    @Column(name = "return_item_id")
    private UUID returnItemId;

    @Column(name = "return_id")
    private UUID returnId;

    @Column(name = "variant_id")
    private UUID variantId;

    private Integer quantity;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}