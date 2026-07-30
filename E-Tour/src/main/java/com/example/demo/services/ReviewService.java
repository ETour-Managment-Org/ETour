package com.example.demo.services;

import java.util.List;

import com.example.demo.dto.review.ReviewRequestDTO;
import com.example.demo.dto.review.ReviewResponseDTO;
import com.example.demo.dto.review.ReviewSummaryDTO;

public interface ReviewService {

    ReviewResponseDTO submitReview(ReviewRequestDTO request, String username);

    ReviewSummaryDTO getTourReviews(Integer tourId);

    List<ReviewResponseDTO> getMyReviews(String username);

    ReviewResponseDTO updateReview(Integer reviewId, ReviewRequestDTO request, String username);

    void deleteReview(Integer reviewId, String username);

    List<Integer> getReviewableBookingIds(String username);
}
