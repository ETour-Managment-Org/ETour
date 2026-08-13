package com.etour.common;

import java.time.LocalDate;

import com.etour.entities.Booking;
import com.etour.entities.Schedule;
import com.etour.entities.Tour;

public final class TourDates {
    private TourDates() {
    }

    public static LocalDate returnDate(Schedule schedule, Tour tour) {
        if (schedule == null || schedule.getStartDate() == null) {
            return null;
        }

        LocalDate departure = schedule.getStartDate();
        Integer days = tour != null ? tour.getDays() : null;

        if (days == null || days < 1) {
            return departure;
        }

        return departure.plusDays(days - 1L);
    }

    public static LocalDate returnDate(Booking booking) {
        if (booking == null) {
            return null;
        }
        return returnDate(booking.getSchedule(), booking.getTour());
    }

    public static boolean hasFinished(Booking booking, LocalDate today) {
        LocalDate end = returnDate(booking);
        return end != null && end.isBefore(today);
    }
}
