package com.api.Reviews.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class ReviewRequestDTO {
    private UUID productId;
    private UUID storeId;
    private Integer rating;
    private String title;
    private String body;
}