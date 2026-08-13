package com.etour.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryDTO {
    private Integer itineraryId;
    private Integer dayNumber;
    private String description;
    private String location;
}
