package com.example.demo.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.example.demo.entity.PassengerDetail;

public interface PassengerService {

       ResponseEntity<PassengerDetail> addPassenger(PassengerDetail passengerDetail);

	ResponseEntity<List<PassengerDetail>> retrivePassenger();

	ResponseEntity<PassengerDetail> retrivePassengers(int id);

}
