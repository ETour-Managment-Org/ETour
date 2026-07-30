package com.example.demo.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDetailDTO {

    private Integer tourId;
    private String tourName;
    private String destination;
    private Integer days;
    private Integer nights;
    private String description;
    private Float price;
    private String location;
    private String durationLabel;

    private Double averageRating;
    private Long reviewCount;

    private Integer categoryId;
    private String categoryName;
    private Integer subCategoryId;
    private String subCategoryName;

    private String stayAndMeals;
    private String addOns;
    private String passportAndVisa;
    private String weather;
    private String doAndDont;

    @Builder.Default
    private List<ItineraryDTO> itineraries = new ArrayList<>();

    @Builder.Default
    private List<ScheduleDTO> schedules = new ArrayList<>();

    @Builder.Default
    private List<CostDTO> costs = new ArrayList<>();

    @Builder.Default
    private List<TourImageDTO> images = new ArrayList<>();
}
