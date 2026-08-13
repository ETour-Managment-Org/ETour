package com.etour.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.common.BookingStatus;
import com.etour.common.RoleName;
import com.etour.common.CityNames;
import com.etour.dto.CostDTO;
import com.etour.dto.ItineraryDTO;
import com.etour.dto.ScheduleDTO;
import com.etour.dto.TourDetailDTO;
import com.etour.dto.admin.AdminBookingDTO;
import com.etour.dto.admin.AdminUserDTO;
import com.etour.dto.admin.BulkImportDTO;
import com.etour.dto.admin.CostRequestDTO;
import com.etour.dto.admin.ItineraryRequestDTO;
import com.etour.dto.admin.ScheduleRequestDTO;
import com.etour.dto.admin.TourRequestDTO;
import com.etour.entities.Booking;
import com.etour.entities.Cancellation;
import com.etour.entities.Category;
import com.etour.entities.Cost;
import com.etour.entities.Itinerary;
import com.etour.entities.Payment;
import com.etour.entities.Review;
import com.etour.entities.Schedule;
import com.etour.entities.SubCategoryMaster;
import com.etour.entities.Tour;
import com.etour.entities.TourImages;
import com.etour.entities.User;
import com.etour.entities.TourCity;
import com.etour.exceptions.ResourceNotFoundException;
import com.etour.repositories.BookingRepository;
import com.etour.repositories.CancellationRepository;
import com.etour.repositories.CategoryRepository;
import com.etour.repositories.CostRepository;
import com.etour.repositories.ItineraryRepository;
import com.etour.repositories.PaymentRepository;
import com.etour.repositories.ReviewRepository;
import com.etour.repositories.ScheduleRepository;
import com.etour.repositories.SubCategoryRepository;
import com.etour.repositories.TourImagesRepository;
import com.etour.repositories.TourRepository;
import com.etour.repositories.UserRepository;

@Service
public class AdminServiceImpl implements AdminService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AdminServiceImpl.class);

    private final TourRepository tourRepository;
    private final TourImagesRepository tourImagesRepository;
    private final CostRepository costRepository;
    private final ItineraryRepository itineraryRepository;
    private final ScheduleRepository scheduleRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;
    private final CancellationRepository cancellationRepository;
    private final UserRepository userRepository;
    private final TourService tourService;

    public AdminServiceImpl(TourRepository tourRepository,
                            TourImagesRepository tourImagesRepository,
                            CostRepository costRepository,
                            ItineraryRepository itineraryRepository,
                            ScheduleRepository scheduleRepository,
                            CategoryRepository categoryRepository,
                            SubCategoryRepository subCategoryRepository,
                            BookingRepository bookingRepository,
                            ReviewRepository reviewRepository,
                            PaymentRepository paymentRepository,
                            CancellationRepository cancellationRepository,
                            UserRepository userRepository,
                            TourService tourService) {
        this.tourRepository = tourRepository;
        this.tourImagesRepository = tourImagesRepository;
        this.costRepository = costRepository;
        this.itineraryRepository = itineraryRepository;
        this.scheduleRepository = scheduleRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.paymentRepository = paymentRepository;
        this.cancellationRepository = cancellationRepository;
        this.userRepository = userRepository;
        this.tourService = tourService;
    }

    @Override
    public BulkImportDTO.Response importTours(List<TourRequestDTO> rows) {
        List<BulkImportDTO.RowResult> results = new ArrayList<>();
        int imported = 0;
        for (int i = 0; i < rows.size(); i++) {
            TourRequestDTO r = rows.get(i);
            int rowNumber = i + 2;
            String name = r == null ? null : r.getTourName();
            try {
                if (r == null || name == null || name.isBlank()) {
                    throw new IllegalArgumentException("Tour name is empty");
                }
                if (tourRepository.existsByTourNameIgnoreCase(name.trim())) {
                    results.add(BulkImportDTO.RowResult.builder()
                            .rowNumber(rowNumber).tourName(name).success(false)
                            .message("Skipped - a tour with this name already exists")
                            .build());
                    continue;
                }
                TourDetailDTO created = createTour(r);
                imported++;
                results.add(BulkImportDTO.RowResult.builder()
                        .rowNumber(rowNumber).tourName(name).success(true)
                        .tourId(created.getTourId())
                        .message("Imported")
                        .build());
            } catch (Exception e) {
                log.warn("Tour import failed at sheet row {} ({}): {}",
                        rowNumber, name, e.getMessage());
                results.add(BulkImportDTO.RowResult.builder()
                        .rowNumber(rowNumber).tourName(name).success(false)
                        .message(e.getMessage() == null ? "Could not import this row" : e.getMessage())
                        .build());
            }
        }
        return BulkImportDTO.Response.builder()
                .total(rows.size())
                .imported(imported)
                .failed(rows.size() - imported)
                .rows(results)
                .build();
    }
    @Override
    @Transactional
    public TourDetailDTO createTour(TourRequestDTO r) {
        Tour tour = new Tour();
        applyTour(tour, r);
        Tour saved = tourRepository.save(tour);
        applyPrimaryImage(saved, r.getPrimaryImageUrl());
        return tourService.getTourDetails(saved.getTourId());
    }
    @Override
    @Transactional
    public TourDetailDTO updateTour(Integer tourId, TourRequestDTO r) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));
        applyTour(tour, r);
        tourRepository.save(tour);
        applyPrimaryImage(tour, r.getPrimaryImageUrl());
        return tourService.getTourDetails(tourId);
    }
    private void applyPrimaryImage(Tour tour, String url) {
        if (url == null || url.isBlank()) {
            return;
        }

        List<TourImages> existing = tourImagesRepository.findByTour_TourId(tour.getTourId());
        TourImages primary = existing.stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                .findFirst()
                .orElseGet(() -> TourImages.builder()
                        .tour(tour)
                        .isPrimary(Boolean.TRUE)
                        .build());
        primary.setTour(tour);
        primary.setSource(url.trim());
        primary.setImageTitle(tour.getTourName());
        primary.setIsPrimary(Boolean.TRUE);
        primary.setUploadDate(LocalDateTime.now());

        tourImagesRepository.save(primary);
    }
    @Override
    @Transactional
    public void deleteTour(Integer tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));
        if (bookingRepository.existsByTour_TourId(tourId)) {
            long count = bookingRepository.countByTour_TourId(tourId);
            log.warn("Refused to delete tour {} - it has {} booking(s)", tourId, count);
            throw new IllegalStateException(
                    "Tour " + tourId + " has " + count + " booking(s) against it and cannot be "
                  + "deleted. Set its departures to CLOSED instead, so it can no longer be "
                  + "booked while the booking history is preserved.");
        }

        List<Review> reviews = reviewRepository.findByTour_TourIdOrderByReviewDateDesc(tourId);
        if (!reviews.isEmpty()) {
            log.info("Tour {} has {} review(s) with no cascade - removing them first",
                    tourId, reviews.size());
            reviewRepository.deleteAll(reviews);
            reviewRepository.flush();
            tour.getReviews().clear();
        }
        tourRepository.delete(tour);
        log.info("Tour {} deleted", tourId);
    }
    private void applyTour(Tour tour, TourRequestDTO r) {
        tour.setTourName(r.getTourName());
        tour.setDestination(r.getDestination());
        tour.setDays(r.getDays());
        tour.setNights(r.getNights());
        tour.setDescription(r.getDescription());
        tour.setPrice(r.getPrice());
        tour.setLocation(r.getLocation());
        tour.setTourType(r.getTourType());
        syncCities(tour);
        tour.setStayAndMeals(r.getStayAndMeals());
        tour.setAddOns(r.getAddOns());
        tour.setPassportAndVisa(r.getPassportAndVisa());
        tour.setWeather(r.getWeather());
        tour.setDoAndDont(r.getDoAndDont());

        if (r.getCategoryId() != null) {
            Category category = categoryRepository.findById(r.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", r.getCategoryId()));
            tour.setCategory(category);
        }
        if (r.getSubCategoryId() != null) {
            SubCategoryMaster sub = subCategoryRepository.findById(r.getSubCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "SubCategory", r.getSubCategoryId()));
            tour.setSubCategory(sub);
        }
    }
    @Override
    @Transactional
    public CostDTO createCost(CostRequestDTO r) {
        Cost cost = new Cost();
        cost.setTour(requireTour(r.getTourId()));
        applyCost(cost, r);
        return toCostDTO(costRepository.save(cost));
    }
    @Override
    @Transactional
    public CostDTO updateCost(Integer costId, CostRequestDTO r) {
        Cost cost = costRepository.findById(costId)
                .orElseThrow(() -> new ResourceNotFoundException("Cost", costId));
        if (r.getTourId() != null) {
            cost.setTour(requireTour(r.getTourId()));
        }
        applyCost(cost, r);
        return toCostDTO(costRepository.save(cost));
    }
    @Override
    @Transactional
    public void deleteCost(Integer costId) {
        Cost cost = costRepository.findById(costId)
                .orElseThrow(() -> new ResourceNotFoundException("Cost", costId));
        costRepository.delete(cost);
    }
    @Override
    @Transactional(readOnly = true)
    public List<CostDTO> getCostsForTour(Integer tourId) {
        return costRepository.findByTour_TourId(tourId)
                .stream().map(this::toCostDTO).collect(Collectors.toList());
    }
    private void applyCost(Cost cost, CostRequestDTO r) {
        cost.setAdultPrice(r.getAdultPrice());
        cost.setSinglePersonPrice(r.getSinglePersonPrice());
        cost.setExtraPersonPrice(r.getExtraPersonPrice());
        cost.setChildWithBedPrice(r.getChildWithBedPrice());
        cost.setChildWithoutBedPrice(r.getChildWithoutBedPrice());
        cost.setValidFrom(r.getValidFrom());
        cost.setValidTo(r.getValidTo());
        cost.setIsActive(r.getIsActive() == null || r.getIsActive());
    }
    @Override
    @Transactional
    public ItineraryDTO createItinerary(ItineraryRequestDTO r) {
        Itinerary it = new Itinerary();
        it.setTour(requireTour(r.getTourId()));
        it.setDayNumber(r.getDayNumber());
        it.setDescription(r.getDescription());
        it.setLocation(r.getLocation());
        return toItineraryDTO(itineraryRepository.save(it));
    }
    @Override
    @Transactional
    public ItineraryDTO updateItinerary(Integer itineraryId, ItineraryRequestDTO r) {
        Itinerary it = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary", itineraryId));
        if (r.getTourId() != null) {
            it.setTour(requireTour(r.getTourId()));
        }
        it.setDayNumber(r.getDayNumber());
        it.setDescription(r.getDescription());
        it.setLocation(r.getLocation());
        return toItineraryDTO(itineraryRepository.save(it));
    }
    @Override
    @Transactional
    public void deleteItinerary(Integer itineraryId) {
        Itinerary it = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary", itineraryId));
        itineraryRepository.delete(it);
    }
    @Override
    @Transactional(readOnly = true)
    public List<ItineraryDTO> getItinerariesForTour(Integer tourId) {
        return itineraryRepository.findByTour_TourIdOrderByDayNumberAsc(tourId)
                .stream().map(this::toItineraryDTO).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public ScheduleDTO createSchedule(ScheduleRequestDTO r) {
        Schedule s = new Schedule();
        s.setTour(requireTour(r.getTourId()));
        s.setStartDate(r.getStartDate());
        s.setTotalSeats(r.getTotalSeats());
        s.setAvailableSeats(r.getAvailableSeats() != null
                ? r.getAvailableSeats() : r.getTotalSeats());
        s.setStatus(r.getStatus() != null ? r.getStatus() : "OPEN");
        return toScheduleDTO(scheduleRepository.save(s));
    }
    @Override
    @Transactional
    public ScheduleDTO updateSchedule(Integer scheduleId, ScheduleRequestDTO r) {
        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));
        if (r.getTourId() != null) {
            s.setTour(requireTour(r.getTourId()));
        }
        s.setStartDate(r.getStartDate());
        if (r.getTotalSeats() != null) {
            s.setTotalSeats(r.getTotalSeats());
        }
        if (r.getAvailableSeats() != null) {
            s.setAvailableSeats(r.getAvailableSeats());
        }
        if (r.getStatus() != null) {
            s.setStatus(r.getStatus());
        }
        return toScheduleDTO(scheduleRepository.save(s));
    }
    @Override
    @Transactional
    public void deleteSchedule(Integer scheduleId) {
        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));
        boolean booked = s.getTotalSeats() != null && s.getAvailableSeats() != null
                && s.getAvailableSeats() < s.getTotalSeats();
        if (booked) {
            throw new IllegalStateException(
                    "Schedule " + scheduleId + " already has bookings against it. "
                  + "Set its status to CLOSED instead of deleting it.");
        }
        scheduleRepository.delete(s);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleDTO> getSchedulesForTour(Integer tourId) {
        return scheduleRepository.findByTour_TourIdOrderByStartDateAsc(tourId)
                .stream().map(this::toScheduleDTO).collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public List<AdminBookingDTO> getAllBookings() {
        return bookingRepository.findAllOrderByDateDesc()
                .stream().map(this::toAdminBookingDTO).collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public List<AdminBookingDTO> getBookingsByStatus(String status) {
        return bookingRepository.findByBookingStatus(status)
                .stream().map(this::toAdminBookingDTO).collect(Collectors.toList());
    }
    private Tour requireTour(Integer tourId) {
        return tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));
    }
    private CostDTO toCostDTO(Cost c) {
        return CostDTO.builder()
                .costId(c.getCostId())
                .adultPrice(c.getAdultPrice())
                .singlePersonPrice(c.getSinglePersonPrice())
                .extraPersonPrice(c.getExtraPersonPrice())
                .childWithBedPrice(c.getChildWithBedPrice())
                .childWithoutBedPrice(c.getChildWithoutBedPrice())
                .validFrom(c.getValidFrom())
                .validTo(c.getValidTo())
                .isActive(c.getIsActive())
                .build();
    }
    private ItineraryDTO toItineraryDTO(Itinerary i) {
        return ItineraryDTO.builder()
                .itineraryId(i.getItineraryId())
                .dayNumber(i.getDayNumber())
                .description(i.getDescription())
                .location(i.getLocation())
                .build();
    }
    private ScheduleDTO toScheduleDTO(Schedule s) {
        return ScheduleDTO.builder()
                .scheduleId(s.getScheduleId())
                .startDate(s.getStartDate())
                .availableSeats(s.getAvailableSeats())
                .totalSeats(s.getTotalSeats())
                .status(s.getStatus())
                .build();
    }
    private AdminBookingDTO toAdminBookingDTO(Booking b) {
        List<Payment> payments = paymentRepository.findByBooking_BookingId(b.getBookingId());
        Payment latest = payments.isEmpty() ? null : payments.get(payments.size() - 1);
        Cancellation cancellation = cancellationRepository
                .findByBooking_BookingId(b.getBookingId()).orElse(null);
        String customerName = null;
        String customerEmail = null;
        if (b.getUser() != null) {
            String fn = b.getUser().getFirstName();
            String ln = b.getUser().getLastName();
            customerName = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
            if (customerName.isEmpty()) {
                customerName = b.getUser().getUsername();
            }
            customerEmail = b.getUser().getEmail();
        }
        return AdminBookingDTO.builder()
                .bookingId(b.getBookingId())
                .bookingDate(b.getBookingDate())
                .bookingStatus(b.getBookingStatus())
                .customerId(b.getUser() != null ? b.getUser().getUserId() : null)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .tourId(b.getTour() != null ? b.getTour().getTourId() : null)
                .tourName(b.getTour() != null ? b.getTour().getTourName() : null)
                .scheduleId(b.getSchedule() != null ? b.getSchedule().getScheduleId() : null)
                .departureDate(b.getSchedule() != null ? b.getSchedule().getStartDate() : null)
                .noOfPax(b.getPassengers() != null ? b.getPassengers().size() : null)
                .totalAmount(b.getTotalAmount())
                .paymentStatus(latest != null ? latest.getPaymentStatus() : null)
                .paymentMethod(latest != null ? latest.getPaymentMethod() : null)
                .refundAmount(cancellation != null ? cancellation.getRefundAmount() : null)
                .refundStatus(cancellation != null ? cancellation.getRefundStatus() : null)
                .build();
    }
    private void syncCities(Tour tour) {
        tour.getCities().clear();
        int order = 1;
        for (String name : CityNames.split(tour.getDestination())) {
            tour.getCities().add(TourCity.builder()
                    .tour(tour)
                    .cityName(name)
                    .stopOrder(order++)
                    .build());
        }
    }
    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toAdminUserDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public AdminUserDTO getUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return toAdminUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminBookingDTO> getBookingsForUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return bookingRepository.findByUser_UserIdOrderByBookingIdAsc(userId)
                .stream().map(this::toAdminBookingDTO).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public AdminUserDTO setUserActive(Integer userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole() != null && RoleName.ADMIN.equals(user.getRole().getRoleName()) && !active) {
            throw new IllegalArgumentException("An administrator account cannot be deactivated");
        }
        user.setIsactive(active);
        userRepository.save(user);
        return toAdminUserDTO(user);
    }
    @Override
    @Transactional(readOnly = true)
    public List<TourDetailDTO> getAllToursForAdmin() {
        return tourRepository.findAll().stream()
                .map(t -> {
                    TourDetailDTO dto = tourService.getTourDetails(t.getTourId());

                    dto.setBookingCount(bookingRepository.countByTour_TourId(t.getTourId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    private AdminUserDTO toAdminUserDTO(User user) {
        List<Booking> bookings =
                bookingRepository.findByUser_UserIdOrderByBookingIdAsc(user.getUserId());
        long cancelled = bookings.stream()
                .filter(b -> BookingStatus.CANCELLED.equals(b.getBookingStatus()))
                .count();
        long active = bookings.stream()
                .filter(b -> !BookingStatus.CANCELLED.equals(b.getBookingStatus())
                          && !BookingStatus.COMPLETED.equals(b.getBookingStatus()))
                .count();
        BigDecimal spent = bookings.stream()
                .filter(b -> !BookingStatus.CANCELLED.equals(b.getBookingStatus()))
                .map(Booking::getTotalAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String fn = user.getFirstName() != null ? user.getFirstName() : "";
        String ln = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (fn + " " + ln).trim();
        if (fullName.isEmpty()) {
            fullName = user.getUsername();
        }
        return AdminUserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(fullName)
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .city(user.getCity())
                .gender(user.getGender())
                .role(user.getRole() != null ? user.getRole().getRoleName() : null)
                .active(user.getIsactive())
                .createdAt(user.getCreatedAt())
                .totalBookings((long) bookings.size())
                .activeBookings(active)
                .cancelledBookings(cancelled)
                .totalSpent(spent)
                .build();
    }
}
