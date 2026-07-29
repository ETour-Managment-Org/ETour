package com.tourism.demo.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tourism.demo.entity.PassengerDetail;

public interface PassengerService {

       ResponseEntity<PassengerDetail> addPassenger(PassengerDetail passengerDetail);

	ResponseEntity<List<PassengerDetail>> retrivePassenger();

	ResponseEntity<PassengerDetail> retrivePassengers(int id);

}
