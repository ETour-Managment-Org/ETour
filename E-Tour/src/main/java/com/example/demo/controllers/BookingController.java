package com.example.demo.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.common.RoleName;
import com.example.demo.dto.booking.BookingRequestDTO;
import com.example.demo.dto.booking.BookingResponseDTO;
import com.example.demo.dto.booking.CancellationRequestDTO;
import com.example.demo.dto.booking.CancellationResponseDTO;
import com.example.demo.services.BookingService;
import com.example.demo.services.CancellationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final CancellationService cancellationService;

    public BookingController(BookingService bookingService,
                             CancellationService cancellationService) {
        this.bookingService = bookingService;
        this.cancellationService = cancellationService;
    }

    @PostMapping("/place")
    public ResponseEntity<BookingResponseDTO> placeBooking(
            @Valid @RequestBody BookingRequestDTO request,
            Authentication authentication) {

        BookingResponseDTO response =
                bookingService.placeBooking(request, authentication.getName());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponseDTO>> myBookings(Authentication authentication) {
        return new ResponseEntity<>(
                bookingService.getMyBookings(authentication.getName()), HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBooking(@PathVariable Integer bookingId,
                                                        Authentication authentication) {
        boolean isAdmin = hasAdminRole(authentication);
        return new ResponseEntity<>(
                bookingService.getBooking(bookingId, authentication.getName(), isAdmin),
                HttpStatus.OK);
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(RoleName.ROLE_ADMIN::equals);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<CancellationResponseDTO> cancelBooking(
            @PathVariable Integer bookingId,
            @Valid @RequestBody CancellationRequestDTO request,
            Authentication authentication) {

        boolean isAdmin = hasAdminRole(authentication);

        CancellationResponseDTO response = cancellationService.cancelBooking(
                bookingId, request, authentication.getName(), isAdmin);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
