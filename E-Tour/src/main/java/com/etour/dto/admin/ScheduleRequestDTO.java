package com.etour.dto.admin;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRequestDTO {
    @NotNull(message = "tourId is required")
    private Integer tourId;

    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    private Integer totalSeats;

    private Integer availableSeats;

    @Builder.Default
    private String status = "OPEN";
}
