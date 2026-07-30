package com.example.demo.services;

import com.example.demo.dto.booking.CancellationRequestDTO;
import com.example.demo.dto.booking.CancellationResponseDTO;

public interface CancellationService {

    CancellationResponseDTO cancelBooking(Integer bookingId,
                                          CancellationRequestDTO request,
                                          String username,
                                          boolean isAdmin);
}
