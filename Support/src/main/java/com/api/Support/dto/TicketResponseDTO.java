package com.api.Support.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.api.Support.enums.TicketStatus;

import lombok.Data;

@Data
public class TicketResponseDTO {
    private UUID ticketId;
    private UUID userId;
    private String subject;
    private TicketStatus status;
    private OffsetDateTime createdAt;
}