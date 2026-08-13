package com.etour.services;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "etour.payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGatewayService implements PaymentGatewayService {
    private static final Logger log = LoggerFactory.getLogger(MockPaymentGatewayService.class);
    private static String ref(String prefix) {
        return prefix + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase();
    }

    @Override
    public String provider() {
        return "mock";
    }
    @Override
    public OrderResult createOrder(BigDecimal amount, String receipt) {
        return new OrderResult("mock", ref("order_MOCK"), "mock_key",
                amount.movePointRight(2).longValueExact(), "INR", receipt);
    }
    @Override
    public PaymentResult charge(BigDecimal amount, String paymentMethod, Integer bookingReference) {
        String reference = ref("MOCK-");
        log.info("[MOCK PAYMENT] Charged {} via {} for booking {} -> {}",
                amount, paymentMethod, bookingReference, reference);
        return new PaymentResult(true, reference,
                "Mock payment accepted. No real transaction was performed.");
    }
    @Override
    public PaymentResult verify(VerificationRequest request, BigDecimal expectedAmount) {
        log.info("[MOCK PAYMENT] Verify called - nothing to check, accepting {}", request.paymentId());
        return new PaymentResult(true, request.paymentId(), "Mock payment accepted.");
    }
}
