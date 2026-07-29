package com.tourism.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourism.demo.entity.PassengerDetail;

public interface PassengerRepository extends JpaRepository<PassengerDetail,Integer> {

}
