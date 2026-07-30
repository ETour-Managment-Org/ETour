package com.example.demo.dto.review;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryDTO {

    private Integer tourId;

    private Double averageRating;

    private Long totalReviews;

    @Builder.Default
    private List<RatingBucketDTO> distribution = new ArrayList<>();

    @Builder.Default
    private List<ReviewResponseDTO> reviews = new ArrayList<>();
}
