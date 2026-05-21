package com.api.Support.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class MessageResponseDTO {
    private UUID messageId;
    private UUID ticketId;
    private UUID senderId;
    private String message;
    private OffsetDateTime createdAt;
}