package com.tourism.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourism.demo.entity.BookingHeader;

public interface BookingRepository extends JpaRepository<BookingHeader,Integer>{

}
