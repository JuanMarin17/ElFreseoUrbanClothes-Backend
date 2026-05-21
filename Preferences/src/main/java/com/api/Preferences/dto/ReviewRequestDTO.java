package com.api.Preferences.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewRequestDTO {
    private UUID productId;
    private Integer rating;
    private String title;
    private String body;
}