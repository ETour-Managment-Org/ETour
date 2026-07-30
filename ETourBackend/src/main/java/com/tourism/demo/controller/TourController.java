package com.tourism.demo.controller;

import com.tourism.demo.dto.request.TourRequestDTO;
import com.tourism.demo.dto.response.TourDTO;
import com.tourism.demo.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    
    @PostMapping
    public ResponseEntity<TourDTO> createTour(@RequestBody TourRequestDTO tourRequestDTO) {
        TourDTO createdTour = tourService.createTour(tourRequestDTO);
        return new ResponseEntity<>(createdTour, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TourDTO>> getAllTours() {
        List<TourDTO> tours = tourService.getAllTours();
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDTO> getTourById(@PathVariable("id") Integer tourId) {
        TourDTO tour = tourService.getTourById(tourId);
        return ResponseEntity.ok(tour);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourDTO> updateTour(
            @PathVariable("id") Integer tourId,
            @RequestBody TourRequestDTO tourRequestDTO) {
        TourDTO updatedTour = tourService.updateTour(tourId, tourRequestDTO);
        return ResponseEntity.ok(updatedTour);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTour(@PathVariable("id") Integer tourId) {
        tourService.deleteTour(tourId);
        return ResponseEntity.ok("Tour deleted successfully with ID: " + tourId);
    }
}