package com.api.Support.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.api.Support.enums.TicketStatus;

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
@Table(name = "support_ticket")
public class SupportTicket {

    @Id
    @GeneratedValue
    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "user_id")
    private UUID userId;

    private String subject;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.OPEN;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}