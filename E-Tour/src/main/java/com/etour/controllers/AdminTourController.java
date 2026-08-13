package com.etour.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etour.dto.CostDTO;
import com.etour.dto.ItineraryDTO;
import com.etour.dto.ScheduleDTO;
import com.etour.dto.TourDetailDTO;
import com.etour.dto.admin.AdminBookingDTO;
import com.etour.dto.admin.AdminUserDTO;
import com.etour.dto.feedback.FeedbackResponseDTO;
import com.etour.services.AuthService;
import com.etour.services.FeedbackService;
import com.etour.dto.admin.BulkImportDTO;
import com.etour.dto.admin.CostRequestDTO;
import com.etour.dto.admin.ItineraryRequestDTO;
import com.etour.dto.admin.ScheduleRequestDTO;
import com.etour.dto.admin.TourRequestDTO;
import com.etour.services.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminTourController {
    private final AdminService adminService;
    private final FeedbackService feedbackService;
    private final AuthService authService;

    public AdminTourController(AdminService adminService,
                              FeedbackService feedbackService,
                              AuthService authService) {
        this.adminService = adminService;
        this.feedbackService = feedbackService;
        this.authService = authService;
    }

    @PostMapping("/tours")
    public ResponseEntity<TourDetailDTO> createTour(@Valid @RequestBody TourRequestDTO request) {
        return new ResponseEntity<>(adminService.createTour(request), HttpStatus.CREATED);
    }

    @PostMapping("/tours/import")
    public ResponseEntity<BulkImportDTO.Response> importTours(
            @RequestBody BulkImportDTO.Request request) {
        List<TourRequestDTO> rows = request.getTours() == null
                ? List.of() : request.getTours();
        return new ResponseEntity<>(adminService.importTours(rows), HttpStatus.OK);
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
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDTO>> allUsers() {
        return new ResponseEntity<>(adminService.getAllUsers(), HttpStatus.OK);
    }
    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDTO> user(@PathVariable Integer userId) {
        return new ResponseEntity<>(adminService.getUser(userId), HttpStatus.OK);
    }
    @GetMapping("/users/{userId}/bookings")
    public ResponseEntity<List<AdminBookingDTO>> userBookings(@PathVariable Integer userId) {
        return new ResponseEntity<>(adminService.getBookingsForUser(userId), HttpStatus.OK);
    }
    @PatchMapping("/users/{userId}/active")
    public ResponseEntity<AdminUserDTO> setUserActive(@PathVariable Integer userId,
                                                      @RequestParam boolean active) {
        return new ResponseEntity<>(adminService.setUserActive(userId, active), HttpStatus.OK);
    }

    @GetMapping("/tours")
    public ResponseEntity<List<TourDetailDTO>> allTours() {
        return new ResponseEntity<>(adminService.getAllToursForAdmin(), HttpStatus.OK);
    }
    @GetMapping("/feedback")
    public ResponseEntity<List<FeedbackResponseDTO>> feedback(
            @RequestParam(required = false) String status) {
        return new ResponseEntity<>(feedbackService.getAll(status), HttpStatus.OK);
    }

    @PatchMapping("/feedback/{feedbackId}/status")
    public ResponseEntity<FeedbackResponseDTO> updateFeedbackStatus(
            @PathVariable Integer feedbackId,
            @RequestParam String status) {
        return new ResponseEntity<>(
                feedbackService.updateStatus(feedbackId, status), HttpStatus.OK);
    }
    @PatchMapping("/users/{userId}/password")
    public ResponseEntity<java.util.Map<String, String>> resetUserPassword(
            @PathVariable Integer userId,
            @RequestBody java.util.Map<String, String> body) {
        authService.resetPassword(userId, body.get("newPassword"));

        return new ResponseEntity<>(
                java.util.Map.of("message", "Password reset."), HttpStatus.OK);
    }
    @PatchMapping("/feedback/{feedbackId}/publish")
    public ResponseEntity<FeedbackResponseDTO> publishFeedback(
            @PathVariable Integer feedbackId,
            @RequestParam boolean published) {
        return new ResponseEntity<>(
                feedbackService.setPublished(feedbackId, published), HttpStatus.OK);
    }
}
