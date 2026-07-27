package com.example.demo.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.example.demo.entity.BookingHeader;

public interface BookingService {

	ResponseEntity<BookingHeader> addBooking(BookingHeader bookingHeader);

	ResponseEntity<List<BookingHeader>> retriveBooking();

	ResponseEntity<BookingHeader> retriveBookingbyId(int id);



}
