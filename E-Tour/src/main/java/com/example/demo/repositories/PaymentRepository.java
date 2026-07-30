package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByBooking_BookingId(Integer bookingId);

    Optional<Payment> findFirstByBooking_BookingIdAndPaymentStatusOrderByPaymentIdDesc(
            Integer bookingId, String paymentStatus);
}
