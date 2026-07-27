package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.PassengerDetail;

public interface PassengerRepository extends JpaRepository<PassengerDetail,Integer> {

}
