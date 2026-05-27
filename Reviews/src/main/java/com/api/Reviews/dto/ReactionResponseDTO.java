package com.api.Reviews.dto;

import java.util.UUID;

import com.api.Reviews.enums.ReactionType;

import lombok.Data;

@Data
public class ReactionResponseDTO {
    private UUID reactionId;
    private UUID reviewId;
    private UUID userId;
    private ReactionType reactionType;
}