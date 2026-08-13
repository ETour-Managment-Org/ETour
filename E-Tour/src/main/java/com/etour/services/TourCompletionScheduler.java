package com.etour.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TourCompletionScheduler {
    private static final Logger log = LoggerFactory.getLogger(TourCompletionScheduler.class);

    private final BookingServiceImpl bookingService;

    public TourCompletionScheduler(BookingServiceImpl bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(cron = "${etour.scheduling.completion-cron:0 0 7 * * *}")
    public void completeFinishedTours() {
        try {
            int completed = bookingService.completeElapsedBookings();
            log.debug("Tour completion sweep finished, {} booking(s) updated", completed);
        } catch (Exception ex) {
            log.error("Tour completion sweep failed: {}", ex.getMessage());
        }
    }
}
