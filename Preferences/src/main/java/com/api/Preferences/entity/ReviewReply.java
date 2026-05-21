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
@Table(name = "review_reply")
public class ReviewReply {

    @Id
    @GeneratedValue
    @Column(name = "reply_id")
    private UUID replyId;

    @Column(name = "review_id")
    private UUID reviewId;

    @Column(name = "user_id")
    private UUID userId;

    private String body;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}