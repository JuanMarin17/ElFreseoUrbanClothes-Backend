package com.api.Reviews.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityReviewPageDTO {

    private List<CommunityReviewResponseDTO> data;
    private long total;
    private int page;
    private int totalPages;
}
