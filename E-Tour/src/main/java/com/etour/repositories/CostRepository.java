package com.etour.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.etour.entities.Cost;

public interface CostRepository extends JpaRepository<Cost, Integer> {
    List<Cost> findByTour_TourIdAndIsActiveTrue(Integer tourId);

    List<Cost> findByTour_TourId(Integer tourId);

    @Query("""
           select c.tour.tourId, min(c.adultPrice)
             from Cost c
            where c.isActive = true
              and c.adultPrice is not null
            group by c.tour.tourId
           """)
    List<Object[]> findMinAdultPricePerTour();
}
