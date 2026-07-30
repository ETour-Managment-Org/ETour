package com.example.demo.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.review.ReviewRequestDTO;
import com.example.demo.dto.review.ReviewResponseDTO;
import com.example.demo.dto.review.ReviewSummaryDTO;
import com.example.demo.services.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> submitReview(
            @Valid @RequestBody ReviewRequestDTO request,
            Authentication authentication) {

        return new ResponseEntity<>(
                reviewService.submitReview(request, authentication.getName()),
                HttpStatus.CREATED);
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<ReviewSummaryDTO> tourReviews(@PathVariable Integer tourId) {
        return new ResponseEntity<>(reviewService.getTourReviews(tourId), HttpStatus.OK);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponseDTO>> myReviews(Authentication authentication) {
        return new ResponseEntity<>(
                reviewService.getMyReviews(authentication.getName()), HttpStatus.OK);
    }

    @GetMapping("/reviewable")
    public ResponseEntity<List<Integer>> reviewableBookings(Authentication authentication) {
        return new ResponseEntity<>(
                reviewService.getReviewableBookingIds(authentication.getName()), HttpStatus.OK);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Integer reviewId,
            @Valid @RequestBody ReviewRequestDTO request,
            Authentication authentication) {

        return new ResponseEntity<>(
                reviewService.updateReview(reviewId, request, authentication.getName()),
                HttpStatus.OK);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer reviewId,
                                             Authentication authentication) {
        reviewService.deleteReview(reviewId, authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
