package com.etour.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entities.TourImages;

public interface TourImagesRepository extends JpaRepository<TourImages, Integer> {
    List<TourImages> findByTour_TourId(Integer tourId);

    List<TourImages> findByIsPrimaryTrue();
}
