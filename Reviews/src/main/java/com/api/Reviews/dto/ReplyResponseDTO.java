package com.api.Reviews.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReplyResponseDTO {
    private UUID replyId;
    private UUID reviewId;
    private UUID userId;
    private String body;
    private OffsetDateTime createdAt;
}