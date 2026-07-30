package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.PassengerDetails;

public interface PassengerRepository extends JpaRepository<PassengerDetails, Integer> {

    List<PassengerDetails> findByBooking_BookingId(Integer bookingId);
}
