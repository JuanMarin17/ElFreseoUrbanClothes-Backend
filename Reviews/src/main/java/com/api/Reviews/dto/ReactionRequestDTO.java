package com.api.Reviews.dto;

import com.api.Reviews.enums.ReactionType;

import lombok.Data;

@Data
public class ReactionRequestDTO {
    private ReactionType reactionType;
}