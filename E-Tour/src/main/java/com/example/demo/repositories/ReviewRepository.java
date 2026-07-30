package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByTour_TourIdOrderByReviewDateDesc(Integer tourId);

    List<Review> findByCustomer_UserIdOrderByReviewDateDesc(Integer userId);

    boolean existsByBooking_BookingId(Integer bookingId);

    Optional<Review> findByBooking_BookingId(Integer bookingId);

    @Query("select avg(r.rating) from Review r where r.tour.tourId = :tourId")
    Double findAverageRating(@Param("tourId") Integer tourId);

    long countByTour_TourId(Integer tourId);

    @Query("""
           select r.rating, count(r)
             from Review r
            where r.tour.tourId = :tourId
            group by r.rating
            order by r.rating desc
           """)
    List<Object[]> findRatingDistribution(@Param("tourId") Integer tourId);

    @Query("select r.tour.tourId, avg(r.rating), count(r) from Review r group by r.tour.tourId")
    List<Object[]> findRatingSummaryPerTour();
}
