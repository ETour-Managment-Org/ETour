package com.etour.pricing;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FareBandPolicy {
    private final int childMaxAge;

    public FareBandPolicy(@Value("${etour.pricing.child-max-age:12}") int childMaxAge) {
        this.childMaxAge = childMaxAge;
    }

    public int calculateAgeAtDeparture(LocalDate birthDate, LocalDate departureDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("birthDate is required for every passenger");
        }
        if (departureDate == null) {
            throw new IllegalArgumentException("departure date is required to price a booking");
        }
        if (birthDate.isAfter(departureDate)) {
            throw new IllegalArgumentException(
                    "birthDate " + birthDate + " is after the departure date " + departureDate);
        }
        return Period.between(birthDate, departureDate).getYears();
    }

    public boolean isChild(int age) {
        return age <= childMaxAge;
    }

    public FareBand resolve(int age, boolean withBed, String string) {
        if (isChild(age)) {
            return withBed ? FareBand.CHILD_WITH_BED : FareBand.CHILD_WITHOUT_BED;
        }
        FareBand requested = FareBand.fromOccupancy(string);
        if (requested == FareBand.SINGLE || requested == FareBand.EXTRA_PERSON) {
            return requested;
        }
        return FareBand.TWIN_SHARING;
    }

    public int getChildMaxAge() {
        return childMaxAge;
    }
}
