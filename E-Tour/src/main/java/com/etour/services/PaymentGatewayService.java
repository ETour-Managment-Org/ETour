package com.etour.services;

import java.math.BigDecimal;

public interface PaymentGatewayService {
    record PaymentResult(boolean success, String transactionReference, String message) {
    }
    record OrderResult(String provider, String orderId, String keyId,
                       long amountPaise, String currency, String receipt) {
    }
    record VerificationRequest(String orderId, String paymentId, String signature) {
    }

    String provider();
    OrderResult createOrder(BigDecimal amount, String receipt);
    PaymentResult charge(BigDecimal amount, String paymentMethod, Integer bookingReference);
    PaymentResult verify(VerificationRequest request, BigDecimal expectedAmount);
}
