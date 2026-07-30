package com.example.demo.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.example.demo.entities.Cost;

@Component
public class TourCostCalculator {

    public BigDecimal rateFor(Cost cost, FareBand band) {
        if (cost == null || band == null) {
            return BigDecimal.ZERO;
        }
        return switch (band) {
            case TWIN_SHARING -> nvl(cost.getAdultPrice());
            case SINGLE -> firstNonNull(cost.getSinglePersonPrice(), cost.getAdultPrice());
            case EXTRA_PERSON -> firstNonNull(cost.getExtraPersonPrice(), cost.getAdultPrice());
            case CHILD_WITH_BED -> firstNonNull(cost.getChildWithBedPrice(), cost.getAdultPrice());
            case CHILD_WITHOUT_BED -> firstNonNull(cost.getChildWithoutBedPrice(), cost.getAdultPrice());
        };
    }

    public BigDecimal twinSharingRate(Cost cost) {
        return cost == null ? BigDecimal.ZERO : nvl(cost.getAdultPrice());
    }

    public boolean isValidOn(Cost cost, LocalDate onDate) {
        if (cost == null || onDate == null) {
            return false;
        }
        boolean fromOk = cost.getValidFrom() == null || !onDate.isBefore(cost.getValidFrom());
        boolean toOk = cost.getValidTo() == null || !onDate.isAfter(cost.getValidTo());
        return fromOk && toOk;
    }

    private BigDecimal firstNonNull(BigDecimal preferred, BigDecimal fallback) {
        if (preferred != null) {
            return preferred;
        }
        return fallback != null ? fallback : BigDecimal.ZERO;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
