package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.PassengerDetail;
import com.example.demo.service.PassengerService;

@RestController
public class PassengerController {
PassengerService passengerService;
	PassengerController(PassengerService passengerService){
		this.passengerService=passengerService;
	}
	
	@PostMapping("/passengerDetail")
	public ResponseEntity<PassengerDetail> postPassenger(@RequestBody PassengerDetail passengerDetail){
		return passengerService.addPassenger(passengerDetail);
	}
	
	@GetMapping("/passengerDetail")
	public ResponseEntity<List<PassengerDetail>> getPassengers(){
		return passengerService.retrivePassenger();
	}
	
	@GetMapping("/passengerDetail/{id}")
	public ResponseEntity<PassengerDetail> getPassenger(@PathVariable int id){
		return passengerService.retrivePassengers(id);
	}
	
}
