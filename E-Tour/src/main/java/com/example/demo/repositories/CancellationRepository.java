package com.example.demo.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.Cancellation;

public interface CancellationRepository extends JpaRepository<Cancellation, Integer> {

    Optional<Cancellation> findByBooking_BookingId(Integer bookingId);

    boolean existsByBooking_BookingId(Integer bookingId);
}
