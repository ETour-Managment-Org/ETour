package com.tourism.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourism.demo.entity.PassengerDetails;

public interface PassengerRepository extends JpaRepository<PassengerDetails,Integer> {

}
