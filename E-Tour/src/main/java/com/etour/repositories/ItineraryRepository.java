package com.etour.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entities.Itinerary;

public interface ItineraryRepository extends JpaRepository<Itinerary, Integer> {
    List<Itinerary> findByTour_TourIdOrderByDayNumberAsc(Integer tourId);
}
