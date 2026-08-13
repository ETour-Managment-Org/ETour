package com.etour.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entities.Cancellation;

public interface CancellationRepository extends JpaRepository<Cancellation, Integer> {
    Optional<Cancellation> findByBooking_BookingId(Integer bookingId);

    boolean existsByBooking_BookingId(Integer bookingId);
}
