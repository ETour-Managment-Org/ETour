package com.tourism.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itinerary {

    private Integer itineraryId;

    private Integer tourId;

    private Integer dayNumber;

    private String description;

    private String location;
}