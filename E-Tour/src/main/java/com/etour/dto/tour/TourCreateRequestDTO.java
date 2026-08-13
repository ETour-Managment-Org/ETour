package com.etour.dto.tour;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourCreateRequestDTO {
    @NotBlank(message = "tourName is required")
    private String tourName;

    private String destination;
    private Integer days;
    private Integer nights;
    private String description;

    private Float price;

    private String location;

    private String tourType;

    private Integer categoryId;
    private Integer subCategoryId;

    private String stayAndMeals;
    private String addOns;
    private String passportAndVisa;
    private String weather;
    private String doAndDont;

    @Valid
    @Builder.Default
    private List<CostCreateDTO> costs = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<ScheduleCreateDTO> schedules = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<ItineraryCreateDTO> itineraries = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<TourImageCreateDTO> images = new ArrayList<>();
}
