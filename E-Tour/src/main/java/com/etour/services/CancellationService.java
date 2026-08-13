package com.etour.services;

import com.etour.dto.booking.CancellationRequestDTO;
import com.etour.dto.booking.CancellationResponseDTO;

public interface CancellationService {
    CancellationResponseDTO cancelBooking(Integer bookingId,
                                          CancellationRequestDTO request,
                                          String username,
                                          boolean isAdmin);
}
