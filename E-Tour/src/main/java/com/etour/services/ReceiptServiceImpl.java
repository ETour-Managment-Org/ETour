package com.etour.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.etour.email.EmailProperties;
import com.etour.email.EmailService;
import com.etour.entities.Booking;

@Service
public class ReceiptServiceImpl implements ReceiptService {
    private static final Logger log = LoggerFactory.getLogger(ReceiptServiceImpl.class);

    private final EmailService emailService;
    private final EmailProperties emailProperties;

    public ReceiptServiceImpl(EmailService emailService, EmailProperties emailProperties) {
        this.emailService = emailService;
        this.emailProperties = emailProperties;
    }

    @Override
    public String generateAndSendReceipt(Booking booking) {
        String recipient = booking.resolveNotificationEmail();
        String tourName = booking.getTour() != null ? booking.getTour().getTourName() : "unknown";

        log.info("Receipt for booking {} ({}) queued for {}",
                booking.getBookingId(), tourName, recipient);

        emailService.sendBookingInvoice(booking);

        if (recipient == null) {
            return "Booking confirmed. No e-mail address on file, so no invoice was sent.";
        }

        if (!emailProperties.isEnabled()) {
            return "Booking confirmed. Your invoice would be e-mailed to " + recipient
                 + " (e-mail delivery is switched off in this environment).";
        }

        return "Booking confirmed. Your invoice has been e-mailed to " + recipient + ".";
    }
}
