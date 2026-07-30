package com.example.demo.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.common.BookingStatus;
import com.example.demo.common.PaymentStatus;
import com.example.demo.dto.booking.BookingRequestDTO;
import com.example.demo.dto.booking.BookingResponseDTO;
import com.example.demo.dto.booking.PassengerDTO;
import com.example.demo.dto.booking.PassengerResponseDTO;
import com.example.demo.entities.Booking;
import com.example.demo.entities.Cost;
import com.example.demo.entities.PassengerDetails;
import com.example.demo.entities.Payment;
import com.example.demo.entities.Schedule;
import com.example.demo.entities.Tour;
import com.example.demo.entities.User;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.pricing.FareBand;
import com.example.demo.pricing.FareBandPolicy;
import com.example.demo.pricing.Occupancy;
import com.example.demo.pricing.TourCostCalculator;
import com.example.demo.repositories.BookingRepository;
import com.example.demo.repositories.CostRepository;
import com.example.demo.repositories.PaymentRepository;
import com.example.demo.repositories.ScheduleRepository;
import com.example.demo.repositories.TourRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.PaymentGatewayService.PaymentResult;

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

    public BookingServiceImpl(BookingRepository bookingRepository,
                              TourRepository tourRepository,
                              ScheduleRepository scheduleRepository,
                              CostRepository costRepository,
                              UserRepository userRepository,
                              PaymentRepository paymentRepository,
                              FareBandPolicy fareBandPolicy,
                              TourCostCalculator costCalculator,
                              PaymentGatewayService paymentGateway,
                              ReceiptService receiptService) {
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

        PaymentResult result = paymentGateway.charge(total, method, saved.getBookingId());

        Payment payment = Payment.builder()
                .booking(saved)
                .amount(total)
                .paymentDate(LocalDate.now())
                .paymentStatus(result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .paymentMethod(method)
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
    @Transactional(readOnly = true)
    public BookingResponseDTO getBooking(Integer bookingId, String username, boolean isAdmin) {
        Booking booking = bookingRepository.findByIdWithPassengers(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!isAdmin) {
            String owner = booking.getUser() != null ? booking.getUser().getUsername() : null;
            if (owner == null || !owner.equals(username)) {
                throw new AccessDeniedException("This booking does not belong to you");
            }
        }
        return toResponse(booking, latestPayment(bookingId), null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getMyBookings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for " + username));

        return bookingRepository.findByUser_UserIdOrderByBookingDateDesc(user.getUserId())
                .stream()
                .map(b -> toResponse(b, latestPayment(b.getBookingId()), null, null, null))
                .collect(Collectors.toList());
    }

    private Payment latestPayment(Integer bookingId) {
        List<Payment> payments = paymentRepository.findByBooking_BookingId(bookingId);
        return payments.isEmpty() ? null : payments.get(payments.size() - 1);
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
}
