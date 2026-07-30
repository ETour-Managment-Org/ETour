package com.tourism.demo.repository;

import com.tourism.demo.entity.Tour;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Integer> {

    @EntityGraph(attributePaths = {"category"})
    List<Tour> findAll();

    @EntityGraph(attributePaths = {"category"})
    Optional<Tour> findById(Integer tourId);

    // Custom query methods
    List<Tour> findByCategoryCategoryId(Integer categoryId);

    List<Tour> findByLocationContainingIgnoreCase(String location);
}