package com.apiSupport.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {

    private UUID messageId;
    private UUID ticketId;
    private UUID senderId;
    private String message;
    private LocalDateTime createdAt;
}