package com.api.Preferences.dto;

import java.util.UUID;

import com.api.Preferences.enums.ReactionType;

import lombok.Data;

@Data
public class ReactionResponseDTO {
    private UUID reactionId;
    private UUID reviewId;
    private UUID userId;
    private ReactionType reactionType;
}