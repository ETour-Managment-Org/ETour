package com.etour.services;

import java.util.List;

import com.etour.dto.booking.BookingRequestDTO;
import com.etour.dto.booking.BookingResponseDTO;

public interface BookingService {
    BookingResponseDTO placeBooking(BookingRequestDTO request, String username);

    BookingResponseDTO getBooking(Integer bookingId, String username, boolean isAdmin);

    List<BookingResponseDTO> getMyBookings(String username);
}
