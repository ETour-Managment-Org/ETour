package com.etour.dto.tour;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCreateDTO {
    @NotNull(message = "startDate is required for every schedule")
    private LocalDate startDate;

    private Integer totalSeats;

    private Integer availableSeats;

    @Builder.Default
    private String status = "OPEN";
}
