package com.example.demo.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.demo.dto.TourDetailDTO;
import com.example.demo.dto.TourListDTO;
import com.example.demo.dto.tour.TourCreateRequestDTO;
import com.example.demo.services.TourService;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @PostMapping
    public ResponseEntity<TourDetailDTO> createTour(
            @Valid @RequestBody TourCreateRequestDTO request) {
        return new ResponseEntity<>(tourService.createTour(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TourListDTO>> getAllTours() {
        return new ResponseEntity<>(tourService.getAllTours(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDetailDTO> getTourById(@PathVariable Integer id) {
        return new ResponseEntity<>(tourService.getTourDetails(id), HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TourListDTO>> getToursByCategory(@PathVariable Integer categoryId) {
        return new ResponseEntity<>(tourService.getToursByCategory(categoryId), HttpStatus.OK);
    }

    @GetMapping("/subcategory/{subCategoryId}")
    public ResponseEntity<List<TourListDTO>> getToursBySubCategory(
            @PathVariable Integer subCategoryId) {
        return new ResponseEntity<>(tourService.getToursBySubCategory(subCategoryId), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TourListDTO>> searchTours(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration) {

        List<TourListDTO> results = tourService.searchTours(
                startDate, endDate, minPrice, maxPrice, minDuration, maxDuration);

        return new ResponseEntity<>(results, HttpStatus.OK);
    }
}
