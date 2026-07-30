package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.TourImages;

public interface TourImagesRepository extends JpaRepository<TourImages, Integer> {

    List<TourImages> findByTour_TourId(Integer tourId);

    List<TourImages> findByIsPrimaryTrue();
}
