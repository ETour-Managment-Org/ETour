package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.BookingHeader;
import com.example.demo.service.BookingService;

@RestController
public class BookingController {

	BookingService bookingService;

	BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@PostMapping("/Booking")
	public ResponseEntity<BookingHeader> PostBooking(@RequestBody BookingHeader bookingHeader) {

		return bookingService.addBooking(bookingHeader);
	}

	@GetMapping("/Booking")
	public ResponseEntity<List<BookingHeader>> GetBooking() {
		// Set the owning side of the relationship

		return bookingService.retriveBooking();
	}
	
	@GetMapping("/Booking/{id}")
	public ResponseEntity<BookingHeader> GetBookingById(@PathVariable int id) {
		// Set the owning side of the relationship

		return bookingService.retriveBookingbyId(id);
	}
}
