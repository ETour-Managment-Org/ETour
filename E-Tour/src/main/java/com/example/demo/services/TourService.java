package com.example.demo.services;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.dto.TourDetailDTO;
import com.example.demo.dto.tour.TourCreateRequestDTO;
import com.example.demo.dto.TourListDTO;

public interface TourService {

    List<TourListDTO> getAllTours();

    TourDetailDTO getTourDetails(Integer tourId);

    TourDetailDTO createTour(TourCreateRequestDTO request);

    List<TourListDTO> getToursByCategory(Integer categoryId);

    List<TourListDTO> getToursBySubCategory(Integer subCategoryId);

    List<TourListDTO> searchTours(LocalDate startDate,
                                  LocalDate endDate,
                                  Double minPrice,
                                  Double maxPrice,
                                  Integer minDuration,
                                  Integer maxDuration);
}
