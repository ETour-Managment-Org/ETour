package com.example.demo.services;

import java.math.BigDecimal;

public interface PaymentGatewayService {

    record PaymentResult(boolean success, String transactionReference, String message) {
    }

    PaymentResult charge(BigDecimal amount, String paymentMethod, Integer bookingReference);
}
