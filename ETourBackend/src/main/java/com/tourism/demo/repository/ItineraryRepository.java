package com.tourism.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

//import com.sun.tools.javac.util.List;
import com.tourism.demo.dto.request.Itinerary;

public interface ItineraryRepository
extends JpaRepository<Itinerary, Integer> {

List<Itinerary> findByTourTourId(Integer tourId);

boolean existsByTourTourIdAndDayNumber(
    Integer tourId,
    Integer dayNumber);
}

