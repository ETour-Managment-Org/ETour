package com.example.demo.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {

    private Integer bookingId;
    private LocalDate bookingDate;
    private String bookingStatus;

    private Integer tourId;
    private String tourName;
    private String destination;

    private Integer scheduleId;
    private LocalDate departureDate;

    private Integer customerId;
    private String customerName;

    private Integer noOfPax;

    private Integer roomsRequired;

    private Integer extraBeds;
    private BigDecimal totalAmount;

    private Integer paymentId;
    private String paymentStatus;
    private String paymentMethod;

    private String receiptMessage;

    @Builder.Default
    private List<PassengerResponseDTO> passengers = new ArrayList<>();
}
