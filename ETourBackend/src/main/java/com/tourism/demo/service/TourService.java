package com.tourism.demo.service;

import com.tourism.demo.dto.request.TourRequestDTO;
import com.tourism.demo.dto.response.TourDTO;
import java.util.List;

public interface TourService {
    TourDTO createTour(TourRequestDTO tourRequestDTO);
    TourDTO updateTour(Integer tourId, TourRequestDTO tourRequestDTO);
    TourDTO getTourById(Integer tourId);
    List<TourDTO> getAllTours();
    void deleteTour(Integer tourId);
}