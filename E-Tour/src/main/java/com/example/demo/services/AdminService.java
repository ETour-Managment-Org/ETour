package com.example.demo.services;

import java.util.List;

import com.example.demo.dto.CostDTO;
import com.example.demo.dto.ItineraryDTO;
import com.example.demo.dto.ScheduleDTO;
import com.example.demo.dto.TourDetailDTO;
import com.example.demo.dto.admin.AdminBookingDTO;
import com.example.demo.dto.admin.CostRequestDTO;
import com.example.demo.dto.admin.ItineraryRequestDTO;
import com.example.demo.dto.admin.ScheduleRequestDTO;
import com.example.demo.dto.admin.TourRequestDTO;

public interface AdminService {

    TourDetailDTO createTour(TourRequestDTO request);
    TourDetailDTO updateTour(Integer tourId, TourRequestDTO request);
    void deleteTour(Integer tourId);

    CostDTO createCost(CostRequestDTO request);
    CostDTO updateCost(Integer costId, CostRequestDTO request);
    void deleteCost(Integer costId);
    List<CostDTO> getCostsForTour(Integer tourId);

    ItineraryDTO createItinerary(ItineraryRequestDTO request);
    ItineraryDTO updateItinerary(Integer itineraryId, ItineraryRequestDTO request);
    void deleteItinerary(Integer itineraryId);
    List<ItineraryDTO> getItinerariesForTour(Integer tourId);

    ScheduleDTO createSchedule(ScheduleRequestDTO request);
    ScheduleDTO updateSchedule(Integer scheduleId, ScheduleRequestDTO request);
    void deleteSchedule(Integer scheduleId);
    List<ScheduleDTO> getSchedulesForTour(Integer tourId);

    List<AdminBookingDTO> getAllBookings();
    List<AdminBookingDTO> getBookingsByStatus(String status);
}
