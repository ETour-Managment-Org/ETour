package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByUser_UserIdOrderByBookingDateDesc(Integer userId);

    List<Booking> findByBookingStatus(String bookingStatus);

    @Query("select b from Booking b left join fetch b.passengers where b.bookingId = :bookingId")
    Optional<Booking> findByIdWithPassengers(@Param("bookingId") Integer bookingId);

    @Query("select b from Booking b order by b.bookingDate desc, b.bookingId desc")
    List<Booking> findAllOrderByDateDesc();
}
