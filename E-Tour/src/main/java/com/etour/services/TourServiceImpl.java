package com.etour.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.common.CategoryScope;
import com.etour.common.CityNames;
import com.etour.dto.CostDTO;
import com.etour.dto.ItineraryDTO;
import com.etour.dto.ScheduleDTO;
import com.etour.dto.TourDetailDTO;
import com.etour.dto.TourImageDTO;
import com.etour.dto.TourListDTO;
import com.etour.dto.tour.CostCreateDTO;
import com.etour.dto.tour.ItineraryCreateDTO;
import com.etour.dto.tour.ScheduleCreateDTO;
import com.etour.dto.tour.TourCreateRequestDTO;
import com.etour.dto.tour.TourImageCreateDTO;
import com.etour.entities.Category;
import com.etour.entities.Cost;
import com.etour.entities.SubCategoryMaster;
import com.etour.entities.Itinerary;
import com.etour.entities.Schedule;
import com.etour.entities.Tour;
import com.etour.entities.TourCity;
import com.etour.entities.TourImages;
import com.etour.exceptions.ResourceNotFoundException;
import com.etour.repositories.CategoryRepository;
import com.etour.repositories.CostRepository;
import com.etour.repositories.SubCategoryRepository;
import com.etour.repositories.ItineraryRepository;
import com.etour.repositories.ScheduleRepository;
import com.etour.repositories.TourImagesRepository;
import com.etour.repositories.ReviewRepository;
import com.etour.repositories.TourRepository;
import com.etour.services.TourService;

@Service
public class TourServiceImpl implements TourService {
    private final TourRepository tourRepository;
    private final ScheduleRepository scheduleRepository;
    private final CostRepository costRepository;
    private final ItineraryRepository itineraryRepository;
    private final TourImagesRepository tourImagesRepository;
    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    public TourServiceImpl(TourRepository tourRepository,
                           ScheduleRepository scheduleRepository,
                           CostRepository costRepository,
                           ItineraryRepository itineraryRepository,
                           TourImagesRepository tourImagesRepository,
                           ReviewRepository reviewRepository,
                           CategoryRepository categoryRepository,
                           SubCategoryRepository subCategoryRepository) {
        this.tourRepository = tourRepository;
        this.scheduleRepository = scheduleRepository;
        this.costRepository = costRepository;
        this.itineraryRepository = itineraryRepository;
        this.tourImagesRepository = tourImagesRepository;
        this.reviewRepository = reviewRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourListDTO> getAllTours() {
        return toListDTOs(tourRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourListDTO> searchTours(LocalDate startDate,
                                         LocalDate endDate,
                                         Double minPrice,
                                         Double maxPrice,
                                         Integer minDuration,
                                         Integer maxDuration,
                                         String city) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("minPrice must not be greater than maxPrice");
        }
        if (minDuration != null && maxDuration != null && minDuration > maxDuration) {
            throw new IllegalArgumentException("minDuration must not be greater than maxDuration");
        }

        List<Tour> tours = tourRepository.searchTours(
                startDate,
                endDate,
                toBigDecimal(minPrice),
                toBigDecimal(maxPrice),
                minDuration,
                maxDuration,
                blankToNull(city));

        return toListDTOs(tours);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourListDTO> getToursByCity(String city) {
        String term = blankToNull(city);
        if (term == null) {
            return List.of();
        }
        return toListDTOs(tourRepository.findByCity(term));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCities() {
        return tourRepository.findAllCityNames();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
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
    public TourDetailDTO getTourDetails(Integer tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));

        List<ItineraryDTO> itineraries =
                itineraryRepository.findByTour_TourIdOrderByDayNumberAsc(tourId)
                        .stream().map(this::toItineraryDTO).collect(Collectors.toList());

        LocalDate today = LocalDate.now();

        List<ScheduleDTO> schedules =
                scheduleRepository.findByTour_TourIdOrderByStartDateAsc(tourId)
                        .stream()
                        .filter(sc -> sc.getStartDate() != null
                                   && !sc.getStartDate().isBefore(today))
                        .map(this::toScheduleDTO).collect(Collectors.toList());

        List<CostDTO> costs =
                costRepository.findByTour_TourId(tourId)
                        .stream().map(this::toCostDTO).collect(Collectors.toList());

        List<TourImageDTO> images =
                tourImagesRepository.findByTour_TourId(tourId)
                        .stream().map(this::toImageDTO).collect(Collectors.toList());

        return TourDetailDTO.builder()
                .tourId(tour.getTourId())
                .tourName(tour.getTourName())
                .destination(tour.getDestination())
                .days(tour.getDays())
                .nights(tour.getNights())
                .description(tour.getDescription())
                .price(tour.getPrice())
                .location(tour.getLocation())
                .tourType(tour.getTourType())
                .durationLabel(durationLabel(tour))
                .averageRating(roundRating(reviewRepository.findAverageRating(tourId)))
                .reviewCount(reviewRepository.countByTour_TourId(tourId))
                .categoryId(tour.getCategory() != null ? tour.getCategory().getCategoryId() : null)
                .categoryName(tour.getCategory() != null ? tour.getCategory().getCategoryName() : null)
                .subCategoryId(tour.getSubCategory() != null ? tour.getSubCategory().getSubcatId() : null)
                .subCategoryName(tour.getSubCategory() != null ? tour.getSubCategory().getSubcatName() : null)
                .stayAndMeals(tour.getStayAndMeals())
                .addOns(tour.getAddOns())
                .passportAndVisa(tour.getPassportAndVisa())
                .weather(tour.getWeather())
                .doAndDont(tour.getDoAndDont())
                .itineraries(itineraries)
                .schedules(schedules)
                .costs(costs)
                .images(images)
                .build();
    }

    @Override
    @Transactional
    public TourDetailDTO createTour(TourCreateRequestDTO request) {
        Tour tour = new Tour();
        tour.setTourName(request.getTourName());
        tour.setDestination(request.getDestination());
        tour.setDays(request.getDays());
        tour.setNights(request.getNights());
        tour.setDescription(request.getDescription());
        tour.setPrice(request.getPrice());
        tour.setLocation(request.getLocation());
        tour.setTourType(request.getTourType());
        syncCities(tour);
        tour.setStayAndMeals(request.getStayAndMeals());
        tour.setAddOns(request.getAddOns());
        tour.setPassportAndVisa(request.getPassportAndVisa());
        tour.setWeather(request.getWeather());
        tour.setDoAndDont(request.getDoAndDont());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", request.getCategoryId()));
            tour.setCategory(category);
        }
        if (request.getSubCategoryId() != null) {
            SubCategoryMaster sub = subCategoryRepository.findById(request.getSubCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "SubCategory", request.getSubCategoryId()));
            tour.setSubCategory(sub);

            if (tour.getCategory() == null) {
                tour.setCategory(sub.getCategory());
            }
        }

        for (CostCreateDTO c : request.getCosts()) {
            Cost cost = new Cost();
            cost.setTour(tour);
            cost.setAdultPrice(c.getAdultPrice());
            cost.setSinglePersonPrice(c.getSinglePersonPrice());
            cost.setExtraPersonPrice(c.getExtraPersonPrice());
            cost.setChildWithBedPrice(c.getChildWithBedPrice());
            cost.setChildWithoutBedPrice(c.getChildWithoutBedPrice());
            cost.setValidFrom(c.getValidFrom());
            cost.setValidTo(c.getValidTo());
            cost.setIsActive(c.getIsActive() == null || c.getIsActive());
            tour.getCosts().add(cost);
        }

        for (ScheduleCreateDTO sc : request.getSchedules()) {
            Schedule schedule = new Schedule();
            schedule.setTour(tour);
            schedule.setStartDate(sc.getStartDate());
            schedule.setTotalSeats(sc.getTotalSeats());
            schedule.setAvailableSeats(sc.getAvailableSeats() != null
                    ? sc.getAvailableSeats() : sc.getTotalSeats());
            schedule.setStatus(sc.getStatus() != null ? sc.getStatus() : "OPEN");
            tour.getSchedules().add(schedule);
        }

        for (ItineraryCreateDTO it : request.getItineraries()) {
            Itinerary itinerary = new Itinerary();
            itinerary.setTour(tour);
            itinerary.setDayNumber(it.getDayNumber());
            itinerary.setDescription(it.getDescription());
            itinerary.setLocation(it.getLocation());
            tour.getItineraries().add(itinerary);
        }

        boolean primaryAssigned = false;
        for (TourImageCreateDTO img : request.getImages()) {
            TourImages image = new TourImages();
            image.setTour(tour);
            image.setSource(img.getSource());
            image.setImageTitle(img.getImageTitle());

            boolean wantsPrimary = Boolean.TRUE.equals(img.getIsPrimary());

            image.setIsPrimary(wantsPrimary && !primaryAssigned);
            if (wantsPrimary && !primaryAssigned) {
                primaryAssigned = true;
            }
            image.setUploadDate(java.time.LocalDateTime.now());
            tour.getImages().add(image);
        }

        if (!primaryAssigned && !tour.getImages().isEmpty()) {
            tour.getImages().get(0).setIsPrimary(Boolean.TRUE);
        }

        Tour saved = tourRepository.save(tour);
        return getTourDetails(saved.getTourId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourListDTO> getToursByCategory(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }

        Set<Integer> scope = CategoryScope.resolve(categoryId, categoryRepository.findAll());

        List<Tour> matches = tourRepository.findAll().stream()
                .filter(t -> t.getCategory() != null
                          && scope.contains(t.getCategory().getCategoryId()))
                .sorted(Comparator.comparing(Tour::getTourId))
                .collect(Collectors.toList());

        return toListDTOs(matches);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourListDTO> getToursBySubCategory(Integer subCategoryId) {
        if (!subCategoryRepository.existsById(subCategoryId)) {
            throw new ResourceNotFoundException("SubCategory", subCategoryId);
        }
        return toListDTOs(tourRepository.findBySubCategory_SubcatId(subCategoryId));
    }

    private List<TourListDTO> toListDTOs(List<Tour> tours) {
        Map<Integer, BigDecimal> minPriceByTour = new HashMap<>();
        for (Object[] row : costRepository.findMinAdultPricePerTour()) {
            minPriceByTour.put((Integer) row[0], (BigDecimal) row[1]);
        }

        Map<Integer, Double> avgByTour = new HashMap<>();
        Map<Integer, Long> countByTour = new HashMap<>();
        for (Object[] row : reviewRepository.findRatingSummaryPerTour()) {
            Integer tid = (Integer) row[0];
            avgByTour.put(tid, roundRating((Double) row[1]));
            countByTour.put(tid, (Long) row[2]);
        }

        Map<Integer, String> primaryImageByTour = new HashMap<>();
        for (TourImages img : tourImagesRepository.findByIsPrimaryTrue()) {
            if (img.getTour() != null) {
                primaryImageByTour.putIfAbsent(img.getTour().getTourId(), img.getSource());
            }
        }

        return tours.stream()
                .map(t -> TourListDTO.builder()
                        .tourId(t.getTourId())
                        .tourName(t.getTourName())
                        .destination(t.getDestination())
                        .tourType(t.getTourType())
                        .days(t.getDays())
                        .nights(t.getNights())
                        .startingPrice(resolveStartingPrice(t, minPriceByTour))
                        .primaryImageUrl(primaryImageByTour.get(t.getTourId()))
                        .durationLabel(durationLabel(t))
                        .averageRating(avgByTour.get(t.getTourId()))
                        .reviewCount(countByTour.getOrDefault(t.getTourId(), 0L))
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal resolveStartingPrice(Tour tour, Map<Integer, BigDecimal> minPriceByTour) {
        BigDecimal fromCost = minPriceByTour.get(tour.getTourId());
        if (fromCost != null) {
            return fromCost;
        }
        return tour.getPrice() != null ? BigDecimal.valueOf(tour.getPrice()) : null;
    }

    private String durationLabel(Tour tour) {
        if (tour.getNights() == null || tour.getDays() == null) {
            return null;
        }
        return tour.getNights() + "N/" + tour.getDays() + "D";
    }

    private Double roundRating(Double value) {
        if (value == null) {
            return null;
        }
        return java.math.BigDecimal.valueOf(value)
                .setScale(1, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
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

    private TourImageDTO toImageDTO(TourImages i) {
        return TourImageDTO.builder()
                .imageId(i.getImageId())
                .source(i.getSource())
                .imageTitle(i.getImageTitle())
                .isPrimary(i.getIsPrimary())
                .uploadDate(i.getUploadDate())
                .build();
    }
}
