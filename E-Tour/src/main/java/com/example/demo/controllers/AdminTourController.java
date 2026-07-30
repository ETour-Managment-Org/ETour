package com.example.demo.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CostDTO;
import com.example.demo.dto.ItineraryDTO;
import com.example.demo.dto.ScheduleDTO;
import com.example.demo.dto.TourDetailDTO;
import com.example.demo.dto.admin.AdminBookingDTO;
import com.example.demo.dto.admin.CostRequestDTO;
import com.example.demo.dto.admin.ItineraryRequestDTO;
import com.example.demo.dto.admin.ScheduleRequestDTO;
import com.example.demo.dto.admin.TourRequestDTO;
import com.example.demo.services.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminTourController {

    private final AdminService adminService;

    public AdminTourController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/tours")
    public ResponseEntity<TourDetailDTO> createTour(@Valid @RequestBody TourRequestDTO request) {
        return new ResponseEntity<>(adminService.createTour(request), HttpStatus.CREATED);
    }

    @PutMapping("/tours/{tourId}")
    public ResponseEntity<TourDetailDTO> updateTour(@PathVariable Integer tourId,
                                                    @Valid @RequestBody TourRequestDTO request) {
        return new ResponseEntity<>(adminService.updateTour(tourId, request), HttpStatus.OK);
    }

    @DeleteMapping("/tours/{tourId}")
    public ResponseEntity<Void> deleteTour(@PathVariable Integer tourId) {
        adminService.deleteTour(tourId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/costs")
    public ResponseEntity<CostDTO> createCost(@Valid @RequestBody CostRequestDTO request) {
        return new ResponseEntity<>(adminService.createCost(request), HttpStatus.CREATED);
    }

    @PutMapping("/costs/{costId}")
    public ResponseEntity<CostDTO> updateCost(@PathVariable Integer costId,
                                              @Valid @RequestBody CostRequestDTO request) {
        return new ResponseEntity<>(adminService.updateCost(costId, request), HttpStatus.OK);
    }

    @DeleteMapping("/costs/{costId}")
    public ResponseEntity<Void> deleteCost(@PathVariable Integer costId) {
        adminService.deleteCost(costId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/tours/{tourId}/costs")
    public ResponseEntity<List<CostDTO>> costsForTour(@PathVariable Integer tourId) {
        return new ResponseEntity<>(adminService.getCostsForTour(tourId), HttpStatus.OK);
    }

    @PostMapping("/itineraries")
    public ResponseEntity<ItineraryDTO> createItinerary(
            @Valid @RequestBody ItineraryRequestDTO request) {
        return new ResponseEntity<>(adminService.createItinerary(request), HttpStatus.CREATED);
    }

    @PutMapping("/itineraries/{itineraryId}")
    public ResponseEntity<ItineraryDTO> updateItinerary(
            @PathVariable Integer itineraryId,
            @Valid @RequestBody ItineraryRequestDTO request) {
        return new ResponseEntity<>(
                adminService.updateItinerary(itineraryId, request), HttpStatus.OK);
    }

    @DeleteMapping("/itineraries/{itineraryId}")
    public ResponseEntity<Void> deleteItinerary(@PathVariable Integer itineraryId) {
        adminService.deleteItinerary(itineraryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/tours/{tourId}/itineraries")
    public ResponseEntity<List<ItineraryDTO>> itinerariesForTour(@PathVariable Integer tourId) {
        return new ResponseEntity<>(adminService.getItinerariesForTour(tourId), HttpStatus.OK);
    }

    @PostMapping("/schedules")
    public ResponseEntity<ScheduleDTO> createSchedule(
            @Valid @RequestBody ScheduleRequestDTO request) {
        return new ResponseEntity<>(adminService.createSchedule(request), HttpStatus.CREATED);
    }

    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleDTO> updateSchedule(
            @PathVariable Integer scheduleId,
            @Valid @RequestBody ScheduleRequestDTO request) {
        return new ResponseEntity<>(
                adminService.updateSchedule(scheduleId, request), HttpStatus.OK);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer scheduleId) {
        adminService.deleteSchedule(scheduleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/tours/{tourId}/schedules")
    public ResponseEntity<List<ScheduleDTO>> schedulesForTour(@PathVariable Integer tourId) {
        return new ResponseEntity<>(adminService.getSchedulesForTour(tourId), HttpStatus.OK);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingDTO>> allBookings(
            @RequestParam(required = false) String status) {

        List<AdminBookingDTO> result = (status == null || status.isBlank())
                ? adminService.getAllBookings()
                : adminService.getBookingsByStatus(status);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
