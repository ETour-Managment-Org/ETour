package com.tourism.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tourism.demo.entity.PassengerDetails;
import com.tourism.demo.service.PassengerService;

@RestController
public class PassengerController {
PassengerService passengerService;
	PassengerController(PassengerService passengerService){
		this.passengerService=passengerService;
	}
	
	@PostMapping("/passengerDetail")
	public ResponseEntity<PassengerDetails> postPassenger(@RequestBody PassengerDetails passengerDetail){
		return passengerService.addPassenger(passengerDetail);
	}
	
	@GetMapping("/passengerDetail")
	public ResponseEntity<List<PassengerDetails>> getPassengers(){
		return passengerService.retrivePassenger();
	}
	
	@GetMapping("/passengerDetail/{id}")
	public ResponseEntity<PassengerDetails> getPassenger(@PathVariable int id){
		return passengerService.retrivePassengers(id);
	}
	
}
