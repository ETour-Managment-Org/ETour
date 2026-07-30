package com.example.demo.services;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockPaymentGatewayService implements PaymentGatewayService {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGatewayService.class);

    @Override
    public PaymentResult charge(BigDecimal amount, String paymentMethod, Integer bookingReference) {

        String reference = "MOCK-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase();

        log.info("[MOCK PAYMENT] Charged {} via {} for booking {} -> {}",
                amount, paymentMethod, bookingReference, reference);

        return new PaymentResult(true, reference,
                "Mock payment accepted. No real transaction was performed.");
    }
}
