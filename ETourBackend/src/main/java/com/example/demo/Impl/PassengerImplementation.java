package com.example.demo.Impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.entity.PassengerDetail;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PassengerRepository;
import com.example.demo.service.PassengerService;

public class PassengerImplementation implements PassengerService {

	PassengerRepository passengerRepo;
	
	public PassengerImplementation(PassengerRepository passengerRepo){
		this.passengerRepo=passengerRepo;
	}
	
	@Override
	public ResponseEntity<PassengerDetail> addPassenger(PassengerDetail passengerDetail) {
		// TODO Auto-generated method stub
		return new ResponseEntity<>(passengerRepo.save(passengerDetail),HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<List<PassengerDetail>> retrivePassenger() {
		// TODO Auto-generated method stub
		return new ResponseEntity<>(passengerRepo.findAll(),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<PassengerDetail> retrivePassengers(int id) {
		// TODO Auto-generated method stub
		PassengerDetail passengerDetail=passengerRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException());
		return new ResponseEntity<>(passengerDetail,HttpStatus.OK);
		
	}
	
	
	
	

}
