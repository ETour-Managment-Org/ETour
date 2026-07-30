package com.tourism.demo.service;

import java.util.List;

import org.springframework.http.ResponseEntity;


import com.tourism.demo.entity.PassengerDetails;

public interface PassengerService {

       ResponseEntity<PassengerDetails> addPassenger(PassengerDetails passengerDetail);

	ResponseEntity<List<PassengerDetails>> retrivePassenger();

	ResponseEntity<PassengerDetails> retrivePassengers(int id);

}
