package com.example.demo.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String,Object>> ResourceNahiMilaException(ResourceNotFoundException rnme){
		HashMap<String,Object> hm=new HashMap<>();
		hm.put("message",rnme.getMessage());
		hm.put("code",HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(hm,HttpStatus.CREATED);
	}


}
