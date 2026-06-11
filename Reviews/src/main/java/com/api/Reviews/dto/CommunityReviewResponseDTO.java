package com.api.Reviews.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityReviewResponseDTO {

    private UUID id;
    private String userName;
    private String userEmail;
    private Integer rating;
    private String text;
    private Integer likes;
    private OffsetDateTime createdAt;
}
