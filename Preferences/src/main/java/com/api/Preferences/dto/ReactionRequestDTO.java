package com.api.Preferences.dto;

import com.api.Preferences.enums.ReactionType;

import lombok.Data;

@Data
public class ReactionRequestDTO {
    private ReactionType reactionType;
}