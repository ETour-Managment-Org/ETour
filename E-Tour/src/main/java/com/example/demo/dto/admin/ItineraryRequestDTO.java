package com.example.demo.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryRequestDTO {

    @NotNull(message = "tourId is required")
    private Integer tourId;

    @NotNull(message = "dayNumber is required")
    private Integer dayNumber;

    private String description;
    private String location;
}
