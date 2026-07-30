package com.example.demo.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RefundPolicy {

    private final long fullRefundDays;
    private final long partialRefundDays;
    private final BigDecimal fullRefundPercent;
    private final BigDecimal partialRefundPercent;
    private final BigDecimal noRefundPercent;

    public RefundPolicy(
            @Value("${etour.cancellation.full-refund-days:7}") long fullRefundDays,
            @Value("${etour.cancellation.partial-refund-days:3}") long partialRefundDays,
            @Value("${etour.cancellation.full-refund-percent:100}") BigDecimal fullRefundPercent,
            @Value("${etour.cancellation.partial-refund-percent:50}") BigDecimal partialRefundPercent,
            @Value("${etour.cancellation.no-refund-percent:0}") BigDecimal noRefundPercent) {
        this.fullRefundDays = fullRefundDays;
        this.partialRefundDays = partialRefundDays;
        this.fullRefundPercent = fullRefundPercent;
        this.partialRefundPercent = partialRefundPercent;
        this.noRefundPercent = noRefundPercent;
    }

    public BigDecimal calculateRefund(BigDecimal amountPaid, LocalDate departureDate) {
        if (amountPaid == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal percent = resolvePercent(departureDate);
        return amountPaid
                .multiply(percent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal resolvePercent(LocalDate departureDate) {
        if (departureDate == null) {
            return noRefundPercent;
        }
        long daysNotice = ChronoUnit.DAYS.between(LocalDate.now(), departureDate);

        if (daysNotice >= fullRefundDays) {
            return fullRefundPercent;
        }
        if (daysNotice >= partialRefundDays) {
            return partialRefundPercent;
        }
        return noRefundPercent;
    }

    public String describe(LocalDate departureDate) {
        if (departureDate == null) {
            return "Departure date unknown; no refund applied.";
        }
        long daysNotice = ChronoUnit.DAYS.between(LocalDate.now(), departureDate);
        return "Cancelled " + daysNotice + " day(s) before departure. "
             + resolvePercent(departureDate).stripTrailingZeros().toPlainString()
             + " percent refunded per policy.";
    }
}
