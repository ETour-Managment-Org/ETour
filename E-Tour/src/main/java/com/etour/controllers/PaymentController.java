package com.etour.controllers;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etour.services.PaymentGatewayService;
import com.etour.services.PaymentGatewayService.OrderResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentGatewayService gateway;
    public PaymentController(PaymentGatewayService gateway) {
        this.gateway = gateway;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        return Map.of("provider", gateway.provider());
    }

    public record OrderRequest(
            @NotNull(message = "amount is required")
            @DecimalMin(value = "1.00", message = "amount must be at least 1.00")
            BigDecimal amount,
            Integer tourId) {
    }
    @PostMapping("/order")
    public ResponseEntity<OrderResult> createOrder(@Valid @RequestBody OrderRequest request,
                                                   Authentication authentication) {
        String receipt = "etour_" + authentication.getName() + "_" + System.currentTimeMillis();
        if (receipt.length() > 40) receipt = receipt.substring(0, 40);
        return ResponseEntity.ok(gateway.createOrder(request.amount(), receipt));
    }
}
