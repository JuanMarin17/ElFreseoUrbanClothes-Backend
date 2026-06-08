package com.api.Cms.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class CmsPageResponseDTO {
    private UUID pageId;
    private UUID storeId;
    private UUID userId;
    private String title;
    private String content;
    private List<ProductCardDTO> recommendations;
    private OffsetDateTime createdAt;
}