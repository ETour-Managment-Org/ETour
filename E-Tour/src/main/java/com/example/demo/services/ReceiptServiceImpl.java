package com.example.demo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.entities.Booking;

@Service
public class ReceiptServiceImpl implements ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptServiceImpl.class);

    @Override
    public String generateAndSendReceipt(Booking booking) {

        String recipient = booking.getUser() != null ? booking.getUser().getEmail() : "unknown";
        String tourName = booking.getTour() != null ? booking.getTour().getTourName() : "unknown";

        log.info("========================================================");
        log.info(" [SIMULATED PDF RECEIPT]");
        log.info(" Booking ID    : {}", booking.getBookingId());
        log.info(" Tour          : {}", tourName);
        log.info(" Departure     : {}", booking.getSchedule() != null
                ? booking.getSchedule().getStartDate() : "n/a");
        log.info(" Passengers    : {}", booking.getPassengers() == null
                ? 0 : booking.getPassengers().size());
        log.info(" Total amount  : {}", booking.getTotalAmount());
        log.info(" Status        : {}", booking.getBookingStatus());
        log.info(" Emailed to    : {}", recipient);
        log.info("========================================================");

        return "Receipt for booking " + booking.getBookingId()
             + " generated and emailed to " + recipient
             + " (simulated - PDF generation and SMTP delivery are not yet implemented).";
    }
}
