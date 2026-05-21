package com.api.Support.entity;

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
@Table(name = "support_message")
public class SupportMessage {

    @Id
    @GeneratedValue
    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "sender_id")
    private UUID senderId;

    private String message;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}