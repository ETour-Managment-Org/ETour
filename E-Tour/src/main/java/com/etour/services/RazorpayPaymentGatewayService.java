package com.etour.services;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(name = "etour.payment.provider", havingValue = "razorpay")
public class RazorpayPaymentGatewayService implements PaymentGatewayService {
    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentGatewayService.class);
    private static final String API = "https://api.razorpay.com/v1";
    private final String keyId;
    private final String keySecret;
    private final String currency;
    private final RestClient http;
    public RazorpayPaymentGatewayService(
            @Value("${etour.payment.razorpay.key-id:}") String keyId,
            @Value("${etour.payment.razorpay.key-secret:}") String keySecret,
            @Value("${etour.payment.currency:INR}") String currency) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.currency = currency;

        this.http = RestClient.create(API);

        if (keyId.isBlank() || keySecret.isBlank()) {
            log.error("etour.payment.provider=razorpay but the keys are blank. "
                    + "Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET, or switch "
                    + "etour.payment.provider back to mock.");
        } else if (!keyId.startsWith("rzp_test_")) {
            log.warn("Razorpay key '{}...' is not a rzp_test_ key. This project is "
                    + "meant to run in TEST mode only.", keyId.substring(0, Math.min(12, keyId.length())));
        } else {
            log.info("Razorpay gateway active in TEST mode (key {}...)",
                    keyId.substring(0, Math.min(16, keyId.length())));
        }
    }
    @Override
    public String provider() {
        return "razorpay";
    }
    @Override
    public OrderResult createOrder(BigDecimal amount, String receipt) {
        if (keyId.isBlank() || keySecret.isBlank()) {
            throw new IllegalStateException(
                    "Online payment is not configured on the server. Please try again later.");
        }

        long paise = amount.setScale(2, java.math.RoundingMode.HALF_UP)
                           .movePointRight(2).longValueExact();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", paise);
        body.put("currency", currency);
        body.put("receipt", receipt);
        body.put("payment_capture", 1);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> res = http.post()
                    .uri("/orders")
                    .header(HttpHeaders.AUTHORIZATION, basicAuth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (res == null || res.get("id") == null) {
                throw new IllegalStateException("Razorpay returned no order id.");
            }

            String orderId = String.valueOf(res.get("id"));
            log.info("Razorpay order {} created for {} {} (receipt {})",
                    orderId, amount, currency, receipt);
            return new OrderResult("razorpay", orderId, keyId, paise, currency, receipt);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new IllegalStateException(
                    "Could not start the payment. Please try again in a moment.", e);
        }
    }
    @Override
    public PaymentResult charge(BigDecimal amount, String paymentMethod, Integer bookingReference) {
        return new PaymentResult(false, null,
                "This booking was not paid for. Complete the Razorpay checkout first.");
    }
    @Override
    public PaymentResult verify(VerificationRequest request, BigDecimal expectedAmount) {
        if (request == null || isBlank(request.orderId())
                || isBlank(request.paymentId()) || isBlank(request.signature())) {
            return new PaymentResult(false, null, "Payment details are incomplete.");
        }

        String expected = hmacSha256(request.orderId() + "|" + request.paymentId(), keySecret);
        if (!constantTimeEquals(expected, request.signature())) {
            log.warn("Razorpay signature MISMATCH for order {} / payment {} - rejecting",
                    request.orderId(), request.paymentId());
            return new PaymentResult(false, null,
                    "Payment could not be verified. You have not been charged for this booking.");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> p = http.get()
                    .uri("/payments/{id}", request.paymentId())
                    .header(HttpHeaders.AUTHORIZATION, basicAuth())
                    .retrieve()
                    .body(Map.class);

            if (p == null) {
                return new PaymentResult(false, null, "Payment could not be confirmed.");
            }
            String status = String.valueOf(p.get("status"));
            long paidPaise = Long.parseLong(String.valueOf(p.get("amount")));
            long wantPaise = expectedAmount.setScale(2, java.math.RoundingMode.HALF_UP)
                                           .movePointRight(2).longValueExact();
            if (!"captured".equals(status) && !"authorized".equals(status)) {
                return new PaymentResult(false, null,
                        "Payment status is '" + status + "'. The booking was not confirmed.");
            }
            if (paidPaise != wantPaise) {
                log.warn("Razorpay amount mismatch: paid {} paise, expected {} paise",
                        paidPaise, wantPaise);
                return new PaymentResult(false, null,
                        "The amount paid does not match this booking.");
            }
            log.info("Razorpay payment {} verified and captured ({} paise)",
                    request.paymentId(), paidPaise);
            return new PaymentResult(true, request.paymentId(), "Payment received.");
        } catch (Exception e) {
            log.error("Razorpay payment lookup failed for {}: {}",
                    request.paymentId(), e.getMessage());
            return new PaymentResult(false, null,
                    "Could not confirm the payment with the gateway. "
                  + "If money was deducted it will be refunded automatically.");
        }
    }
    private String basicAuth() {
        String raw = keyId + ":" + keySecret;
        return "Basic " + java.util.Base64.getEncoder()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
    static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable in this JVM", e);
        }
    }

    static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }
    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
