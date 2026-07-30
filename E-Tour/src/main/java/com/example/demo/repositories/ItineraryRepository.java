package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.Itinerary;

public interface ItineraryRepository extends JpaRepository<Itinerary, Integer> {

    List<Itinerary> findByTour_TourIdOrderByDayNumberAsc(Integer tourId);
}
