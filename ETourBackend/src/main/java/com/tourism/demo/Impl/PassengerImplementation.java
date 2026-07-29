package com.tourism.demo.Impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tourism.demo.entity.PassengerDetail;
import com.tourism.demo.exception.ResourceNotFoundException;
import com.tourism.demo.repository.PassengerRepository;
import com.tourism.demo.service.PassengerService;

@Service
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
