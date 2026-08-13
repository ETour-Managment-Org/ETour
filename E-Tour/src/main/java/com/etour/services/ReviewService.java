package com.etour.services;

import java.util.List;

import com.etour.dto.review.ReviewRequestDTO;
import com.etour.dto.review.ReviewResponseDTO;
import com.etour.dto.review.ReviewSummaryDTO;

public interface ReviewService {
    ReviewResponseDTO submitReview(ReviewRequestDTO request, String username);

    ReviewSummaryDTO getTourReviews(Integer tourId);

    List<ReviewResponseDTO> getMyReviews(String username);

    ReviewResponseDTO updateReview(Integer reviewId, ReviewRequestDTO request, String username);

    void deleteReview(Integer reviewId, String username);

    List<Integer> getReviewableBookingIds(String username);
}
