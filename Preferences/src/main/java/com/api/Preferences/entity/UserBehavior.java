package com.api.Preferences.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user_behavior")
public class UserBehavior {

    @Id
    @GeneratedValue
    @Column(name = "behavior_id")
    private UUID behaviorId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}