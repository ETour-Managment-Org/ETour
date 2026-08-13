package com.etour.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.common.BookingStatus;
import com.etour.common.TourDates;
import com.etour.common.PaymentStatus;
import com.etour.dto.booking.BookingRequestDTO;
import com.etour.dto.booking.BookingResponseDTO;
import com.etour.dto.booking.PassengerDTO;
import com.etour.dto.booking.PassengerResponseDTO;
import com.etour.entities.Booking;
import com.etour.entities.Cost;
import com.etour.entities.PassengerDetails;
import com.etour.entities.Payment;
import com.etour.entities.Schedule;
import com.etour.entities.Tour;
import com.etour.entities.User;
import com.etour.email.EmailService;
import com.etour.exceptions.ResourceNotFoundException;
import com.etour.pricing.FareBand;
import com.etour.pricing.FareBandPolicy;
import com.etour.pricing.Occupancy;
import com.etour.pricing.TourCostCalculator;
import com.etour.repositories.BookingRepository;
import com.etour.repositories.CostRepository;
import com.etour.repositories.PaymentRepository;
import com.etour.repositories.ScheduleRepository;
import com.etour.repositories.TourRepository;
import com.etour.repositories.UserRepository;
import com.etour.services.PaymentGatewayService.PaymentResult;

@Service
public class BookingServiceImpl implements BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final ScheduleRepository scheduleRepository;
    private final CostRepository costRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final FareBandPolicy fareBandPolicy;
    private final TourCostCalculator costCalculator;
    private final PaymentGatewayService paymentGateway;
    private final ReceiptService receiptService;
    private final EmailService emailService;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              TourRepository tourRepository,
                              ScheduleRepository scheduleRepository,
                              CostRepository costRepository,
                              UserRepository userRepository,
                              PaymentRepository paymentRepository,
                              FareBandPolicy fareBandPolicy,
                              TourCostCalculator costCalculator,
                              PaymentGatewayService paymentGateway,
                              ReceiptService receiptService,
                              EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
        this.scheduleRepository = scheduleRepository;
        this.costRepository = costRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.fareBandPolicy = fareBandPolicy;
        this.costCalculator = costCalculator;
        this.paymentGateway = paymentGateway;
        this.receiptService = receiptService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public BookingResponseDTO placeBooking(BookingRequestDTO request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found for " + username));

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour", request.getTourId()));

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule", request.getScheduleId()));

        if (schedule.getTour() == null
                || !schedule.getTour().getTourId().equals(tour.getTourId())) {
            throw new IllegalArgumentException(
                    "Schedule " + schedule.getScheduleId()
                  + " does not belong to tour " + tour.getTourId());
        }

        LocalDate departureDate = schedule.getStartDate();
        if (departureDate == null) {
            throw new IllegalStateException(
                    "Schedule " + schedule.getScheduleId() + " has no start date, so the "
                  + "age-based fare (BRD-065) cannot be calculated.");
        }
        if (departureDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Departure " + departureDate + " is in the past");
        }

        int paxCount = request.getPassengers().size();

        Integer available = schedule.getAvailableSeats();
        if (available == null) {
            throw new IllegalStateException(
                    "Schedule " + schedule.getScheduleId() + " has no seat count configured");
        }
        if (available < paxCount) {
            throw new IllegalStateException(
                    "Only " + available + " seat(s) remain on this departure, "
                  + paxCount + " requested");
        }

        Cost cost = costRepository.findByTour_TourIdAndIsActiveTrue(tour.getTourId()).stream()
                .filter(c -> isValidOn(c, departureDate))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active cost configured for tour " + tour.getTourId()
                      + " covering " + departureDate));

        Booking booking = Booking.builder()
                .user(user)
                .tour(tour)
                .schedule(schedule)
                .bookingDate(LocalDate.now())
                .bookingStatus(BookingStatus.PENDING)
                .contactName(trimOrNull(request.getContactName()))
                .contactEmail(trimOrNull(request.getContactEmail()))
                .contactPhone(trimOrNull(request.getContactPhone()))
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<PassengerDetails> passengers = new ArrayList<>();
        int twinSharingCount = 0;
        int singleCount = 0;
        int extraBedCount = 0;

        for (PassengerDTO dto : request.getPassengers()) {
            int age = fareBandPolicy.calculateAgeAtDeparture(dto.getBirthDate(), departureDate);

            boolean withBed = dto.getWithBed() == null || Boolean.TRUE.equals(dto.getWithBed());

            FareBand band = fareBandPolicy.resolve(age, withBed, dto.getOccupancy());
            BigDecimal rate = costCalculator.rateFor(cost, band);

            PassengerDetails pax = PassengerDetails.builder()
                    .booking(booking)
                    .fullName(dto.getFullName())
                    .birthDate(dto.getBirthDate())
                    .age(age)
                    .gender(dto.getGender())
                    .email(dto.getEmail())
                    .passportNumber(dto.getPassportNumber())
                    .paxType(band.name())
                    .build();

            switch (band) {
                case SINGLE -> singleCount++;
                case EXTRA_PERSON, CHILD_WITH_BED -> extraBedCount++;
                case TWIN_SHARING -> twinSharingCount++;
                default -> { }
            }

            passengers.add(pax);
            total = total.add(rate);

            log.debug("Passenger {} age {} -> band {} rate {}",
                    dto.getFullName(), age, band, rate);
        }

        int rooms = Occupancy.roomsRequired(twinSharingCount, singleCount, extraBedCount);
        int bedCapacity = rooms * Occupancy.MAX_OCCUPANTS_PER_ROOM;
        if (paxCount > bedCapacity) {
            throw new IllegalArgumentException(
                    "Occupancy is not valid. A room holds " + Occupancy.MAX_PER_ROOM
                  + " people plus " + Occupancy.MAX_EXTRA_BEDS_PER_ROOM
                  + " extra bed. Adjust the occupancy choices for " + paxCount + " passengers.");
        }

        booking.setPassengers(passengers);
        booking.setTotalAmount(total);

        Booking saved = bookingRepository.save(booking);

        String method = request.getPaymentMethod() != null
                ? request.getPaymentMethod() : "ONLINE";

        boolean gatewayCheckout = notBlank(request.getRazorpayPaymentId())
                              || notBlank(request.getRazorpayOrderId())
                              || notBlank(request.getRazorpaySignature());

        PaymentResult result;
        if (gatewayCheckout) {
            if (!(notBlank(request.getRazorpayPaymentId())
               && notBlank(request.getRazorpayOrderId())
               && notBlank(request.getRazorpaySignature()))) {
                throw new IllegalStateException(
                        "Payment details are incomplete. The booking was not confirmed.");
            }
            method = "RAZORPAY";
            result = paymentGateway.verify(
                    new PaymentGatewayService.VerificationRequest(
                            request.getRazorpayOrderId(),
                            request.getRazorpayPaymentId(),
                            request.getRazorpaySignature()),
                    total);
        } else {
            result = paymentGateway.charge(total, method, saved.getBookingId());
        }

        Payment payment = Payment.builder()
                .booking(saved)
                .amount(total)
                .paymentDate(LocalDate.now())
                .paymentStatus(result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .paymentMethod(method)
                .transactionRef(result.transactionReference())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if (!result.success()) {
            throw new IllegalStateException("Payment failed: " + result.message());
        }

        saved.setBookingStatus(BookingStatus.CONFIRMED);
        schedule.setAvailableSeats(available - paxCount);
        scheduleRepository.save(schedule);

        String receiptMessage = receiptService.generateAndSendReceipt(saved);

        return toResponse(saved, savedPayment, receiptMessage, rooms, extraBedCount);
    }

    @Override
    @Transactional
    public BookingResponseDTO getBooking(Integer bookingId, String username, boolean isAdmin) {
        Booking booking = bookingRepository.findByIdWithPassengers(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!isAdmin) {
            String owner = booking.getUser() != null ? booking.getUser().getUsername() : null;
            if (owner == null || !owner.equals(username)) {
                throw new AccessDeniedException("This booking does not belong to you");
            }
        }
        applyElapsedCompletion(booking);

        BookingResponseDTO dto = toResponse(booking, latestPayment(bookingId), null, null, null);

        if (booking.getUser() != null) {
            long rank = bookingRepository.countByUser_UserIdAndBookingIdLessThanEqual(
                    booking.getUser().getUserId(), booking.getBookingId());
            dto.setCustomerBookingNumber((int) rank);
        }

        return dto;
    }

    private void applyElapsedCompletion(Booking booking) {
        if (booking == null || booking.getSchedule() == null) {
            return;
        }

        LocalDate returnDate = TourDates.returnDate(booking);
        if (returnDate == null) {
            return;
        }

        String status = booking.getBookingStatus();

        boolean eligible = BookingStatus.CONFIRMED.equals(status);

        if (eligible && returnDate.isBefore(LocalDate.now())) {
            booking.setBookingStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);

            emailService.sendTourCompletedEmail(booking);
            log.debug("Booking {} returned on {} and was marked COMPLETED",
                    booking.getBookingId(), returnDate);
        }
    }

    @Override
    @Transactional
    public List<BookingResponseDTO> getMyBookings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for " + username));

        List<Booking> ordered =
                bookingRepository.findByUser_UserIdOrderByBookingIdAsc(user.getUserId());

        List<BookingResponseDTO> result = new ArrayList<>();

        for (int index = 0; index < ordered.size(); index++) {
            Booking b = ordered.get(index);
            applyElapsedCompletion(b);
            BookingResponseDTO dto =
                    toResponse(b, latestPayment(b.getBookingId()), null, null, null);
            dto.setCustomerBookingNumber(index + 1);
            result.add(dto);
        }

        Collections.reverse(result);
        return result;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private Payment latestPayment(Integer bookingId) {
        List<Payment> payments = paymentRepository.findByBooking_BookingId(bookingId);
        return payments.isEmpty() ? null : payments.get(payments.size() - 1);
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isValidOn(Cost cost, LocalDate date) {
        boolean fromOk = cost.getValidFrom() == null || !date.isBefore(cost.getValidFrom());
        boolean toOk = cost.getValidTo() == null || !date.isAfter(cost.getValidTo());
        return fromOk && toOk;
    }

    private BookingResponseDTO toResponse(Booking b, Payment payment, String receiptMessage,
                                          Integer rooms, Integer extraBeds) {
        LocalDate departureDate = b.getSchedule() != null ? b.getSchedule().getStartDate() : null;

        int paxCount = b.getPassengers() == null ? 0 : b.getPassengers().size();

        Cost costRow = (b.getTour() != null && departureDate != null)
                ? costRepository.findByTour_TourIdAndIsActiveTrue(b.getTour().getTourId())
                    .stream().filter(c -> isValidOn(c, departureDate)).findFirst().orElse(null)
                : null;

        List<PassengerResponseDTO> pax = new ArrayList<>();
        int twinSharingCount = 0;
        int singleCount = 0;
        int extraBedCount = 0;

        if (b.getPassengers() != null) {
            for (PassengerDetails p : b.getPassengers()) {
                Integer age = p.getAge();

                FareBand band = FareBand.fromOccupancy(p.getPaxType());
                if (band == null && age != null) {
                    band = fareBandPolicy.resolve(age, true, null);
                }

                if (band != null) {
                    switch (band) {
                        case SINGLE -> singleCount++;
                        case EXTRA_PERSON, CHILD_WITH_BED -> extraBedCount++;
                        case TWIN_SHARING -> twinSharingCount++;
                        default -> { }
                    }
                }

                pax.add(PassengerResponseDTO.builder()
                        .paxId(p.getPaxId())
                        .fullName(p.getFullName())
                        .birthDate(p.getBirthDate())
                        .ageAtDeparture(age)
                        .paxType(band != null ? band.name() : null)
                        .paxTypeLabel(band != null ? band.getLabel() : null)
                        .paxAmount(band != null ? costCalculator.rateFor(costRow, band) : null)
                        .gender(p.getGender())
                        .passportNumber(p.getPassportNumber())
                        .email(p.getEmail())
                        .build());
            }
        }

        Integer effectiveRooms = rooms != null
                ? rooms
                : Occupancy.roomsRequired(twinSharingCount, singleCount, extraBedCount);

        Integer effectiveExtraBeds = extraBeds != null ? extraBeds : extraBedCount;

        String customerName = null;
        if (b.getUser() != null) {
            String fn = b.getUser().getFirstName();
            String ln = b.getUser().getLastName();
            customerName = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
            if (customerName.isEmpty()) {
                customerName = b.getUser().getUsername();
            }
        }

        return BookingResponseDTO.builder()
                .bookingId(b.getBookingId())
                .bookingDate(b.getBookingDate())
                .bookingStatus(b.getBookingStatus())
                .tourId(b.getTour() != null ? b.getTour().getTourId() : null)
                .tourName(b.getTour() != null ? b.getTour().getTourName() : null)
                .destination(b.getTour() != null ? b.getTour().getDestination() : null)
                .scheduleId(b.getSchedule() != null ? b.getSchedule().getScheduleId() : null)
                .departureDate(departureDate)
                .customerId(b.getUser() != null ? b.getUser().getUserId() : null)
                .customerName(customerName)
                .noOfPax(paxCount)
                .roomsRequired(effectiveRooms)
                .extraBeds(effectiveExtraBeds)
                .totalAmount(b.getTotalAmount())
                .paymentId(payment != null ? payment.getPaymentId() : null)
                .paymentStatus(payment != null ? payment.getPaymentStatus() : null)
                .paymentMethod(payment != null ? payment.getPaymentMethod() : null)
                .receiptMessage(receiptMessage)
                .passengers(pax)
                .build();
    }

    @Transactional
    public int completeElapsedBookings() {
        int completed = 0;
        for (Booking booking : bookingRepository.findByBookingStatus(BookingStatus.CONFIRMED)) {
            String before = booking.getBookingStatus();
            applyElapsedCompletion(booking);
            if (!before.equals(booking.getBookingStatus())) {
                completed++;
            }
        }
        if (completed > 0) {
            log.info("Marked {} booking(s) as COMPLETED and sent the review invitation", completed);
        }
        return completed;
    }
}
