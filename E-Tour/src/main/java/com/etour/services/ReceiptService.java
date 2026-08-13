package com.etour.services;

import com.etour.entities.Booking;

public interface ReceiptService {
    String generateAndSendReceipt(Booking booking);
}
