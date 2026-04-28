package com.apiSupport.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class MessageRequest {
    private UUID senderId;
    private String message;
}