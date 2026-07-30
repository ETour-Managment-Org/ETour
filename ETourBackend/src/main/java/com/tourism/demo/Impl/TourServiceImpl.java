package com.tourism.demo.Impl;

import com.tourism.demo.dto.request.TourRequestDTO;
import com.tourism.demo.dto.response.TourDTO;
import com.tourism.demo.entity.Category;
import com.tourism.demo.entity.Tour;
import com.tourism.demo.repository.CategoryRepository;
import com.tourism.demo.repository.TourRepository;
import com.tourism.demo.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public TourDTO createTour(TourRequestDTO tourRequestDTO) {
        Category category = null;
        if (tourRequestDTO.getCategoryId() != null) {
            category = categoryRepository.findById(tourRequestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + tourRequestDTO.getCategoryId()));
        }

        // Using standard setters instead of builder
        Tour tour = new Tour();
        tour.setTourName(tourRequestDTO.getTourName());
        tour.setDestination(tourRequestDTO.getDestination());
        tour.setDays(tourRequestDTO.getDays());
        tour.setNights(tourRequestDTO.getNights());
        tour.setDescription(tourRequestDTO.getDescription());
        tour.setPrice(tourRequestDTO.getPrice());
        tour.setLocation(tourRequestDTO.getLocation());
        tour.setCategory(category);

        Tour savedTour = tourRepository.save(tour);
        return mapToDTO(savedTour);
    }

    @Override
    public TourDTO updateTour(Integer tourId, TourRequestDTO tourRequestDTO) {
        Tour existingTour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour not found with id: " + tourId));

        if (tourRequestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(tourRequestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + tourRequestDTO.getCategoryId()));
            existingTour.setCategory(category);
        }

        existingTour.setTourName(tourRequestDTO.getTourName());
        existingTour.setDestination(tourRequestDTO.getDestination());
        existingTour.setDays(tourRequestDTO.getDays());
        existingTour.setNights(tourRequestDTO.getNights());
        existingTour.setDescription(tourRequestDTO.getDescription());
        existingTour.setPrice(tourRequestDTO.getPrice());
        existingTour.setLocation(tourRequestDTO.getLocation());

        Tour updatedTour = tourRepository.save(existingTour);
        return mapToDTO(updatedTour);
    }

    @Override
    public TourDTO getTourById(Integer tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour not found with id: " + tourId));
        return mapToDTO(tour);
    }

    @Override
    public List<TourDTO> getAllTours() {
        return tourRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTour(Integer tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour not found with id: " + tourId));
        tourRepository.delete(tour);
    }

    // Entity to DTO Mapper (Using Builder on Response DTO)
    private TourDTO mapToDTO(Tour tour) {
        return TourDTO.builder()
                .tourId(tour.getTourId())
                .tourName(tour.getTourName())
                .destination(tour.getDestination())
                .days(tour.getDays())
                .nights(tour.getNights())
                .description(tour.getDescription())
                .price(tour.getPrice())
                .location(tour.getLocation())
                .categoryId(tour.getCategory() != null ? tour.getCategory().getCategoryId() : null)
                .categoryName(tour.getCategory() != null ? tour.getCategory().getCategoryName() : null)
                .build();
    }
}