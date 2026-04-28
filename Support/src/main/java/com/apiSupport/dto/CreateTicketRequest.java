package com.apiSupport.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateTicketRequest {
    private UUID userId;
    private String subject;
    private String description;
    private String priority; // lo mandan como string: HIGH, LOW, etc
}