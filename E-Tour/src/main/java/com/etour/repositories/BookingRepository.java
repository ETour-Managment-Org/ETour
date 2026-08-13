package com.etour.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.etour.entities.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    boolean existsByTour_TourId(Integer tourId);

    long countByTour_TourId(Integer tourId);

    List<Booking> findByUser_UserIdOrderByBookingDateDesc(Integer userId);

    List<Booking> findByUser_UserIdOrderByBookingIdAsc(Integer userId);

    long countByUser_UserIdAndBookingIdLessThanEqual(Integer userId, Integer bookingId);

    List<Booking> findByBookingStatus(String bookingStatus);

    @Query("select b from Booking b left join fetch b.passengers where b.bookingId = :bookingId")
    Optional<Booking> findByIdWithPassengers(@Param("bookingId") Integer bookingId);

    @Query("select b from Booking b order by b.bookingDate desc, b.bookingId desc")
    List<Booking> findAllOrderByDateDesc();
}
