package com.tourism.demo.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tourism.demo.entity.Booking;

public interface BookingService {

	ResponseEntity<Booking> addBooking(Booking bookingHeader);

	ResponseEntity<List<Booking>> retriveBooking();

	ResponseEntity<Booking> retriveBookingbyId(int id);



}
