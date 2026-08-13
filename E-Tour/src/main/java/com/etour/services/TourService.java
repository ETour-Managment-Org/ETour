package com.etour.services;

import java.time.LocalDate;
import java.util.List;

import com.etour.dto.TourDetailDTO;
import com.etour.dto.tour.TourCreateRequestDTO;
import com.etour.dto.TourListDTO;

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
                                  Integer maxDuration,
                                  String city);

    List<TourListDTO> getToursByCity(String city);

    List<String> getAllCities();
}
