package com.example.demo.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBookingDTO {

    private Integer bookingId;
    private LocalDate bookingDate;
    private String bookingStatus;

    private Integer customerId;
    private String customerName;
    private String customerEmail;

    private Integer tourId;
    private String tourName;

    private Integer scheduleId;
    private LocalDate departureDate;

    private Integer noOfPax;
    private BigDecimal totalAmount;

    private String paymentStatus;
    private String paymentMethod;

    private BigDecimal refundAmount;
    private String refundStatus;
}
