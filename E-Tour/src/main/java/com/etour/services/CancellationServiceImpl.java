package com.etour.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.common.BookingStatus;
import com.etour.common.PaymentStatus;
import com.etour.common.RefundStatus;
import com.etour.dto.booking.CancellationRequestDTO;
import com.etour.dto.booking.CancellationResponseDTO;
import com.etour.entities.Booking;
import com.etour.entities.Cancellation;
import com.etour.entities.Payment;
import com.etour.email.EmailService;
import com.etour.entities.Schedule;
import com.etour.exceptions.ResourceNotFoundException;
import com.etour.pricing.RefundPolicy;
import com.etour.repositories.BookingRepository;
import com.etour.repositories.CancellationRepository;
import com.etour.repositories.PaymentRepository;
import com.etour.repositories.ScheduleRepository;

@Service
public class CancellationServiceImpl implements CancellationService {
    private static final Logger log = LoggerFactory.getLogger(CancellationServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final CancellationRepository cancellationRepository;
    private final PaymentRepository paymentRepository;
    private final ScheduleRepository scheduleRepository;
    private final RefundPolicy refundPolicy;
    private final EmailService emailService;

    public CancellationServiceImpl(BookingRepository bookingRepository,
                                   CancellationRepository cancellationRepository,
                                   PaymentRepository paymentRepository,
                                   ScheduleRepository scheduleRepository,
                                   RefundPolicy refundPolicy,
                                   EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.cancellationRepository = cancellationRepository;
        this.paymentRepository = paymentRepository;
        this.scheduleRepository = scheduleRepository;
        this.refundPolicy = refundPolicy;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public CancellationResponseDTO cancelBooking(Integer bookingId,
                                                 CancellationRequestDTO request,
                                                 String username,
                                                 boolean isAdmin) {
        Booking booking = bookingRepository.findByIdWithPassengers(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!isAdmin) {
            String owner = booking.getUser() != null ? booking.getUser().getUsername() : null;
            if (owner == null || !owner.equals(username)) {
                throw new AccessDeniedException("This booking does not belong to you");
            }
        }

        if (BookingStatus.CANCELLED.equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Booking " + bookingId + " is already cancelled");
        }
        if (BookingStatus.COMPLETED.equals(booking.getBookingStatus())) {
            throw new IllegalStateException(
                    "Booking " + bookingId + " has already travelled and cannot be cancelled");
        }
        if (cancellationRepository.existsByBooking_BookingId(bookingId)) {
            throw new IllegalStateException(
                    "A cancellation record already exists for booking " + bookingId);
        }

        LocalDate departureDate = booking.getSchedule() != null
                ? booking.getSchedule().getStartDate() : null;

        BigDecimal amountPaid = booking.getTotalAmount() != null
                ? booking.getTotalAmount() : BigDecimal.ZERO;

        BigDecimal refundAmount = refundPolicy.calculateRefund(amountPaid, departureDate);

        booking.setBookingStatus(BookingStatus.CANCELLED);

        Cancellation cancellation = Cancellation.builder()
                .booking(booking)
                .cancellationDate(LocalDateTime.now())
                .reason(request != null ? request.getReason() : "Not specified")
                .refundAmount(refundAmount)
                .refundStatus(refundAmount.signum() > 0
                        ? RefundStatus.PROCESSED : RefundStatus.NOT_APPLICABLE)
                .remarks(refundPolicy.describe(departureDate))
                .build();

        Cancellation savedCancellation = cancellationRepository.save(cancellation);

        if (refundAmount.signum() > 0) {
            List<Payment> payments = paymentRepository.findByBooking_BookingId(bookingId);
            payments.stream()
                    .filter(p -> PaymentStatus.SUCCESS.equals(p.getPaymentStatus()))
                    .forEach(p -> {
                        p.setPaymentStatus(PaymentStatus.REFUNDED);
                        paymentRepository.save(p);
                    });
        }

        Schedule schedule = booking.getSchedule();
        if (schedule != null && schedule.getAvailableSeats() != null) {
            int released = booking.getPassengers() == null ? 0 : booking.getPassengers().size();
            int restored = schedule.getAvailableSeats() + released;

            if (schedule.getTotalSeats() != null && restored > schedule.getTotalSeats()) {
                restored = schedule.getTotalSeats();
            }
            schedule.setAvailableSeats(restored);
            scheduleRepository.save(schedule);

            log.info("Released {} seat(s) back to schedule {}", released, schedule.getScheduleId());
        }

        bookingRepository.save(booking);

        emailService.sendCancellationEmail(booking,
                savedCancellation.getRefundAmount(),
                savedCancellation.getRemarks());

        return CancellationResponseDTO.builder()
                .cancellationId(savedCancellation.getCancellationId())
                .bookingId(bookingId)
                .bookingStatus(booking.getBookingStatus())
                .cancellationDate(savedCancellation.getCancellationDate())
                .reason(savedCancellation.getReason())
                .amountPaid(amountPaid)
                .refundAmount(refundAmount)
                .refundStatus(savedCancellation.getRefundStatus())
                .remarks(savedCancellation.getRemarks())
                .build();
    }
}
