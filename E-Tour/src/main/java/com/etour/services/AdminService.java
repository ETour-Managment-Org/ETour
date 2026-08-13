package com.etour.services;

import java.util.List;

import com.etour.dto.CostDTO;
import com.etour.dto.ItineraryDTO;
import com.etour.dto.ScheduleDTO;
import com.etour.dto.TourDetailDTO;
import com.etour.dto.admin.AdminBookingDTO;
import com.etour.dto.admin.AdminUserDTO;
import com.etour.dto.admin.BulkImportDTO;
import com.etour.dto.admin.CostRequestDTO;
import com.etour.dto.admin.ItineraryRequestDTO;
import com.etour.dto.admin.ScheduleRequestDTO;
import com.etour.dto.admin.TourRequestDTO;

public interface AdminService {
    TourDetailDTO createTour(TourRequestDTO request);

    BulkImportDTO.Response importTours(List<TourRequestDTO> rows);
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

    List<AdminUserDTO> getAllUsers();
    AdminUserDTO getUser(Integer userId);
    List<AdminBookingDTO> getBookingsForUser(Integer userId);
    AdminUserDTO setUserActive(Integer userId, boolean active);

    List<TourDetailDTO> getAllToursForAdmin();
}
