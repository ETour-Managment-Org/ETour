package com.tourism.demo.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tourism.demo.entity.BookingHeader;

public interface BookingService {

	ResponseEntity<BookingHeader> addBooking(BookingHeader bookingHeader);

	ResponseEntity<List<BookingHeader>> retriveBooking();

	ResponseEntity<BookingHeader> retriveBookingbyId(int id);



}
