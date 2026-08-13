package com.etour.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.etour.common.BookingStatus;
import com.etour.dto.booking.BookingRequestDTO;
import com.etour.dto.booking.BookingResponseDTO;
import com.etour.dto.booking.PassengerDTO;
import com.etour.email.EmailService;
import com.etour.entities.Booking;
import com.etour.entities.Cost;
import com.etour.entities.Payment;
import com.etour.entities.Role;
import com.etour.entities.Schedule;
import com.etour.entities.Tour;
import com.etour.entities.User;
import com.etour.exceptions.ResourceNotFoundException;
import com.etour.pricing.FareBandPolicy;
import com.etour.pricing.TourCostCalculator;
import com.etour.repositories.BookingRepository;
import com.etour.repositories.CostRepository;
import com.etour.repositories.PaymentRepository;
import com.etour.repositories.ScheduleRepository;
import com.etour.repositories.TourRepository;
import com.etour.repositories.UserRepository;
import com.etour.services.PaymentGatewayService.PaymentResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl")
class BookingServiceImplTest {
    @Mock private BookingRepository bookingRepository;
    @Mock private TourRepository tourRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private CostRepository costRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentGatewayService paymentGateway;
    @Mock private ReceiptService receiptService;
    @Mock private EmailService emailService;

    private final FareBandPolicy fareBandPolicy = new FareBandPolicy(12);
    private final TourCostCalculator costCalculator = new TourCostCalculator();

    private BookingServiceImpl service;

    private User customer;
    private Tour tour;
    private Schedule schedule;
    private Cost cost;

    private static final LocalDate DEPARTURE = LocalDate.now().plusMonths(6);

    @BeforeEach
    void setUp() {
        service = new BookingServiceImpl(
                bookingRepository, tourRepository, scheduleRepository, costRepository,
                userRepository, paymentRepository, fareBandPolicy, costCalculator,
                paymentGateway, receiptService, emailService);

        Role role = Role.builder().roleId(1).roleName("CUSTOMER").build();

        customer = User.builder()
                .userId(2).username("Aditya").email("aditya@example.com")
                .firstName("Aditya").lastName("Mali").role(role).isactive(true)
                .build();

        tour = Tour.builder()
                .tourId(1).tourName("Kashmir Paradise")
                .destination("Srinagar - Gulmarg - Pahalgam").days(6).nights(5)
                .build();

        schedule = Schedule.builder()
                .scheduleId(1).tour(tour).startDate(DEPARTURE)
                .availableSeats(18).totalSeats(40).status("OPEN")
                .build();

        cost = Cost.builder()
                .costId(1).tour(tour)
                .adultPrice(new BigDecimal("24999.00"))
                .singlePersonPrice(new BigDecimal("36248.00"))
                .extraPersonPrice(new BigDecimal("21249.00"))
                .childWithBedPrice(new BigDecimal("18749.00"))
                .childWithoutBedPrice(new BigDecimal("16999.00"))
                .validFrom(LocalDate.now().minusYears(1))
                .validTo(LocalDate.now().plusYears(2))
                .isActive(true)
                .build();
    }

    private void givenEverythingResolves() {
        when(userRepository.findByUsername("Aditya")).thenReturn(Optional.of(customer));
        when(tourRepository.findById(1)).thenReturn(Optional.of(tour));
        when(scheduleRepository.findById(1)).thenReturn(Optional.of(schedule));
        when(costRepository.findByTour_TourIdAndIsActiveTrue(1)).thenReturn(List.of(cost));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(100);
            return b;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentGateway.charge(any(BigDecimal.class), anyString(), anyInt()))
                .thenReturn(new PaymentResult(true, "TXN-TEST-1", "Approved"));
        when(receiptService.generateAndSendReceipt(any(Booking.class)))
                .thenReturn("Receipt generated");
    }

    private PassengerDTO passenger(String name, LocalDate dob, String occupancy, boolean withBed) {
        return PassengerDTO.builder()
                .fullName(name).birthDate(dob).gender("Male")
                .occupancy(occupancy).withBed(withBed)
                .build();
    }

    private BookingRequestDTO request(PassengerDTO... passengers) {
        return BookingRequestDTO.builder()
                .tourId(1).scheduleId(1).paymentMethod("UPI")
                .passengers(List.of(passengers))
                .build();
    }

    @Nested
    @DisplayName("pricing")
    class Pricing {
        @Test
        @DisplayName("two adults on twin sharing are charged twice the adult rate")
        void twoAdultsTwinSharing() {
            givenEverythingResolves();

            BookingResponseDTO result = service.placeBooking(
                    request(passenger("Aditya Mali", LocalDate.of(1998, 4, 18), "TWIN_SHARING", true),
                            passenger("Sanika Mali", LocalDate.of(1999, 11, 3), "TWIN_SHARING", true)),
                    "Aditya");

            assertThat(result.getTotalAmount()).isEqualByComparingTo("49998.00");
            assertThat(result.getNoOfPax()).isEqualTo(2);
            assertThat(result.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("a child under 13 on the departure date is charged the child rate")
        void childIsChargedChildRate() {
            givenEverythingResolves();

            LocalDate childDob = DEPARTURE.minusYears(8);

            BookingResponseDTO result = service.placeBooking(
                    request(passenger("Aditya Mali", LocalDate.of(1998, 4, 18), "TWIN_SHARING", true),
                            passenger("Baby Mali", childDob, "TWIN_SHARING", true)),
                    "Aditya");

            assertThat(result.getTotalAmount()).isEqualByComparingTo("43748.00");
        }

        @Test
        @DisplayName("age is taken on the departure date, not the booking date")
        void ageIsCalculatedAtDeparture() {
            givenEverythingResolves();

            LocalDate turnsThirteenJustBeforeDeparture = DEPARTURE.minusYears(13).plusDays(1);

            BookingResponseDTO result = service.placeBooking(
                    request(passenger("Nearly Thirteen", turnsThirteenJustBeforeDeparture,
                                      "TWIN_SHARING", true)),
                    "Aditya");

            assertThat(result.getTotalAmount()).isEqualByComparingTo("24999.00");
        }

        @Test
        @DisplayName("single occupancy is charged the single rate")
        void singleOccupancy() {
            givenEverythingResolves();

            BookingResponseDTO result = service.placeBooking(
                    request(passenger("Solo Traveller", LocalDate.of(1990, 1, 1), "SINGLE", true)),
                    "Aditya");

            assertThat(result.getTotalAmount()).isEqualByComparingTo("36248.00");
            assertThat(result.getRoomsRequired()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("occupancy")
    class Occupancy {
        @Test
        @DisplayName("three travellers with an extra bed need one room")
        void threeWithExtraBedIsOneRoom() {
            givenEverythingResolves();

            BookingResponseDTO result = service.placeBooking(
                    request(passenger("A", LocalDate.of(1990, 1, 1), "TWIN_SHARING", true),
                            passenger("B", LocalDate.of(1991, 1, 1), "TWIN_SHARING", true),
                            passenger("C", LocalDate.of(1992, 1, 1), "EXTRA_PERSON", true)),
                    "Aditya");

            assertThat(result.getRoomsRequired()).isEqualTo(1);
            assertThat(result.getExtraBeds()).isEqualTo(1);
        }

        @Test
        @DisplayName("three travellers with a separate room need two rooms")
        void threeWithSeparateRoomIsTwoRooms() {
            givenEverythingResolves();

            BookingResponseDTO result = service.placeBooking(
                    request(passenger("A", LocalDate.of(1990, 1, 1), "TWIN_SHARING", true),
                            passenger("B", LocalDate.of(1991, 1, 1), "TWIN_SHARING", true),
                            passenger("C", LocalDate.of(1992, 1, 1), "SINGLE", true)),
                    "Aditya");

            assertThat(result.getRoomsRequired()).isEqualTo(2);
            assertThat(result.getExtraBeds()).isZero();
        }
    }

    @Nested
    @DisplayName("seat management")
    class Seats {
        @Test
        @DisplayName("seats are reduced by the passenger count once payment succeeds")
        void seatsAreDecremented() {
            givenEverythingResolves();

            service.placeBooking(
                    request(passenger("A", LocalDate.of(1990, 1, 1), "TWIN_SHARING", true),
                            passenger("B", LocalDate.of(1991, 1, 1), "TWIN_SHARING", true)),
                    "Aditya");

            ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
            verify(scheduleRepository).save(captor.capture());

            assertThat(captor.getValue().getAvailableSeats()).isEqualTo(16);
        }

        @Test
        @DisplayName("a booking larger than the remaining seats is rejected")
        void rejectsOverbooking() {
            when(userRepository.findByUsername("Aditya")).thenReturn(Optional.of(customer));
            when(tourRepository.findById(1)).thenReturn(Optional.of(tour));
            when(scheduleRepository.findById(1)).thenReturn(Optional.of(schedule));
            schedule.setAvailableSeats(1);

            assertThatThrownBy(() -> service.placeBooking(
                    request(passenger("A", LocalDate.of(1990, 1, 1), "TWIN_SHARING", true),
                            passenger("B", LocalDate.of(1991, 1, 1), "TWIN_SHARING", true)),
                    "Aditya"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only 1 seat(s) remain");

            verify(bookingRepository, never()).save(any());
            verify(scheduleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("failure paths")
    class Failures {
        @Test
        @DisplayName("an unknown username is rejected before anything else happens")
        void unknownUser() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.placeBooking(
                    request(passenger("A", LocalDate.of(1990, 1, 1), "TWIN_SHARING", true)),
                    "ghost"))
                .isInstanceOf(ResourceNotFoundException.class);

            verify(tourRepository, never()).findById(any());
        }

        @Test
        @DisplayName("a failed payment stops the booking being confirmed")
        void failedPaymentIsRejected() {
            givenEverythingResolves();
            when(paymentGateway.charge(any(BigDecimal.class), anyString(), anyInt()))
                    .thenReturn(new PaymentResult(false, null, "Card declined"));

            assertThatThrownBy(() -> service.placeBooking(
                    request(passenger("A", LocalDate.of(1990, 1, 1), "TWIN_SHARING", true)),
                    "Aditya"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Payment failed");

            verify(scheduleRepository, never()).save(any());
        }

        @Test
        @DisplayName("a departure in the past is rejected")
        void pastDepartureIsRejected() {
            when(userRepository.findByUsername("Aditya")).thenReturn(Optional.of(customer));
            when(tourRepository.findById(1)).thenReturn(Optional.of(tour));
            when(scheduleRepository.findById(1)).thenReturn(Optional.of(schedule));
            schedule.setStartDate(LocalDate.now().minusDays(1));

            assertThatThrownBy(() -> service.placeBooking(
                    request(passenger("A", LocalDate.of(1990, 1, 1), "TWIN_SHARING", true)),
                    "Aditya"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("in the past");
        }
    }
}
