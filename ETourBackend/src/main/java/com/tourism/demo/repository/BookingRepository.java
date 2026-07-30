package com.tourism.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourism.demo.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking,Integer>{

}
