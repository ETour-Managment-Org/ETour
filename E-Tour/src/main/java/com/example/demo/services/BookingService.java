package com.example.demo.services;

import java.util.List;

import com.example.demo.dto.booking.BookingRequestDTO;
import com.example.demo.dto.booking.BookingResponseDTO;

public interface BookingService {

    BookingResponseDTO placeBooking(BookingRequestDTO request, String username);

    BookingResponseDTO getBooking(Integer bookingId, String username, boolean isAdmin);

    List<BookingResponseDTO> getMyBookings(String username);
}
