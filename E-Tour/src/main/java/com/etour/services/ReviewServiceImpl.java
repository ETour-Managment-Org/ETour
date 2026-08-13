package com.etour.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.common.BookingStatus;
import com.etour.common.TourDates;
import com.etour.common.VerificationStatus;
import com.etour.dto.review.RatingBucketDTO;
import com.etour.dto.review.ReviewRequestDTO;
import com.etour.dto.review.ReviewResponseDTO;
import com.etour.dto.review.ReviewSummaryDTO;
import com.etour.entities.Booking;
import com.etour.entities.Review;
import com.etour.entities.User;
import com.etour.exceptions.ResourceNotFoundException;
import com.etour.repositories.BookingRepository;
import com.etour.repositories.ReviewRepository;
import com.etour.repositories.UserRepository;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             BookingRepository bookingRepository,
                             UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ReviewResponseDTO submitReview(ReviewRequestDTO request, String username) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for " + username));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));

        assertOwnedBy(booking, customer);
        assertTravelled(booking);

        if (reviewRepository.existsByBooking_BookingId(booking.getBookingId())) {
            throw new IllegalStateException(
                    "You have already reviewed booking " + booking.getBookingId()
                  + ". Edit the existing review instead.");
        }

        Review review = Review.builder()
                .booking(booking)
                .customer(customer)
                .tour(booking.getTour())
                .rating(request.getRating())
                .reviewTitle(request.getReviewTitle())
                .reviewDescription(request.getReviewDescription())
                .reviewDate(LocalDate.now())

                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        return toDTO(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryDTO getTourReviews(Integer tourId) {
        List<Review> reviews = reviewRepository.findByTour_TourIdOrderByReviewDateDesc(tourId);
        long total = reviews.size();

        Double average = reviewRepository.findAverageRating(tourId);
        if (average != null) {
            average = BigDecimal.valueOf(average)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        Map<Integer, Long> counts = new HashMap<>();
        for (Object[] row : reviewRepository.findRatingDistribution(tourId)) {
            counts.put((Integer) row[0], (Long) row[1]);
        }

        List<RatingBucketDTO> distribution = new ArrayList<>();
        for (int stars = 5; stars >= 1; stars--) {
            long count = counts.getOrDefault(stars, 0L);
            double percentage = total == 0 ? 0d
                    : BigDecimal.valueOf(count * 100.0 / total)
                        .setScale(1, RoundingMode.HALF_UP).doubleValue();

            distribution.add(RatingBucketDTO.builder()
                    .stars(stars)
                    .count(count)
                    .percentage(percentage)
                    .build());
        }

        return ReviewSummaryDTO.builder()
                .tourId(tourId)
                .averageRating(average)
                .totalReviews(total)
                .distribution(distribution)
                .reviews(reviews.stream().map(this::toDTO).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getMyReviews(String username) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for " + username));

        return reviewRepository.findByCustomer_UserIdOrderByReviewDateDesc(customer.getUserId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getReviewableBookingIds(String username) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for " + username));

        return bookingRepository.findByUser_UserIdOrderByBookingDateDesc(customer.getUserId())
                .stream()
                .filter(this::hasTravelled)
                .filter(b -> !reviewRepository.existsByBooking_BookingId(b.getBookingId()))
                .map(Booking::getBookingId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Integer reviewId, ReviewRequestDTO request, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        assertAuthor(review, username);

        review.setRating(request.getRating());
        review.setReviewTitle(request.getReviewTitle());
        review.setReviewDescription(request.getReviewDescription());
        review.setReviewDate(LocalDate.now());

        return toDTO(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(Integer reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        assertAuthor(review, username);
        reviewRepository.delete(review);
    }

    private void assertOwnedBy(Booking booking, User customer) {
        if (booking.getUser() == null
                || !booking.getUser().getUserId().equals(customer.getUserId())) {
            throw new AccessDeniedException("This booking does not belong to you");
        }
    }

    private void assertTravelled(Booking booking) {
        if (BookingStatus.CANCELLED.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("A cancelled booking cannot be reviewed");
        }
        if (BookingStatus.PENDING.equals(booking.getBookingStatus())) {
            throw new IllegalStateException(
                    "Booking " + booking.getBookingId() + " has not been paid for yet");
        }
        if (!hasTravelled(booking)) {
            LocalDate departure = booking.getSchedule() != null
                    ? booking.getSchedule().getStartDate() : null;
            LocalDate returnDate = TourDates.returnDate(booking);

            throw new IllegalStateException(
                    departure != null
                        ? "This tour departs on " + departure + " and returns on " + returnDate
                            + ". You can review it once you are back."
                        : "This booking has no departure date, so it cannot be reviewed yet.");
        }
    }

    private boolean hasTravelled(Booking booking) {
        if (BookingStatus.COMPLETED.equals(booking.getBookingStatus())) {
            return true;
        }
        if (!BookingStatus.CONFIRMED.equals(booking.getBookingStatus())) {
            return false;
        }
        return TourDates.hasFinished(booking, LocalDate.now());
    }

    private void assertAuthor(Review review, String username) {
        String author = review.getCustomer() != null
                ? review.getCustomer().getUsername() : null;
        if (author == null || !author.equals(username)) {
            throw new AccessDeniedException("This review does not belong to you");
        }
    }

    private ReviewResponseDTO toDTO(Review r) {
        String customerName = null;
        if (r.getCustomer() != null) {
            String fn = r.getCustomer().getFirstName();
            String ln = r.getCustomer().getLastName();
            customerName = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
            if (customerName.isEmpty()) {
                customerName = r.getCustomer().getUsername();
            }
        }

        return ReviewResponseDTO.builder()
                .reviewId(r.getReviewId())
                .rating(r.getRating())
                .reviewTitle(r.getReviewTitle())
                .reviewDescription(r.getReviewDescription())
                .reviewDate(r.getReviewDate())
                .verificationStatus(r.getVerificationStatus())
                .tourId(r.getTour() != null ? r.getTour().getTourId() : null)
                .tourName(r.getTour() != null ? r.getTour().getTourName() : null)
                .bookingId(r.getBooking() != null ? r.getBooking().getBookingId() : null)
                .customerName(customerName)
                .build();
    }
}
