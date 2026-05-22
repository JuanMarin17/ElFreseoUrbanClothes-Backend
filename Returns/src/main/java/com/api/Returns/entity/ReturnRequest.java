package com.api.Returns.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.api.Returns.enums.ReturnStatus;

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
@Table(name = "return_request")
public class ReturnRequest {

    @Id
    @GeneratedValue
    @Column(name = "return_id")
    private UUID returnId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "store_id")
    private UUID storeId;

    private String reason;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}