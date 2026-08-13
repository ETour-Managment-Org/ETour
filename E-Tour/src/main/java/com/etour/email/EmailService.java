package com.etour.email;

import com.etour.entities.Booking;
import com.etour.entities.User;

public interface EmailService {
    void sendWelcomeEmail(User user);

    void sendPasswordChangedEmail(User user, boolean changedByAdmin);

    void sendBookingInvoice(Booking booking);

    void sendTourCompletedEmail(Booking booking);

    void sendCancellationEmail(Booking booking, java.math.BigDecimal refundAmount, String policyNote);
}
