package com.example.demo.dto.tour;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryCreateDTO {

    @NotNull(message = "dayNumber is required for every itinerary entry")
    private Integer dayNumber;

    private String description;
    private String location;
}
