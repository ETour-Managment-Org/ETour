package com.etour.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entities.TourCity;

public interface TourCityRepository extends JpaRepository<TourCity, Integer> {
    List<TourCity> findByTour_TourIdOrderByStopOrderAsc(Integer tourId);

    void deleteByTour_TourId(Integer tourId);
}
