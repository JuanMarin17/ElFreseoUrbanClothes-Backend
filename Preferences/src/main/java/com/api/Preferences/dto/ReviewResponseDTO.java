package com.api.Preferences.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReviewResponseDTO {
    private UUID reviewId;
    private UUID productId;
    private UUID userId;
    private Integer rating;
    private String title;
    private String body;
    private Boolean isVerified;
    private OffsetDateTime createdAt;
}