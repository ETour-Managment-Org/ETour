package com.tourism.demo.Impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tourism.demo.entity.BookingHeader;
import com.tourism.demo.entity.PassengerDetail;
import com.tourism.demo.exception.ResourceNotFoundException;
import com.tourism.demo.repository.BookingRepository;
import com.tourism.demo.service.BookingService;

@Service
public class BookingImplementation implements BookingService {
	
	BookingRepository bookingRepo;
	
	BookingImplementation(BookingRepository bookingRepo){
		this.bookingRepo=bookingRepo;
	}

	@Override
	public ResponseEntity<BookingHeader> addBooking(BookingHeader bookingHeader) {
		// TODO Auto-generated method stub
		 // Set the owning side of the relationship
	    for (PassengerDetail passenger
	            : bookingHeader.getPassengers()) {

	        passenger.setBooking(bookingHeader);
	    }

		return new ResponseEntity<>(bookingRepo.save(bookingHeader),HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<List<BookingHeader>> retriveBooking() {
		// TODO Auto-generated method stub
		return new ResponseEntity<>(bookingRepo.findAll(),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<BookingHeader> retriveBookingbyId(int id) {
		// TODO Auto-generated method stub
		BookingHeader bookingHeader=bookingRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException());
		return new ResponseEntity<>(bookingHeader,HttpStatus.OK);
	}

}
