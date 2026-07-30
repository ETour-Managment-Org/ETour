package com.example.demo.services;

import com.example.demo.entities.Booking;

public interface ReceiptService {

    String generateAndSendReceipt(Booking booking);
}
