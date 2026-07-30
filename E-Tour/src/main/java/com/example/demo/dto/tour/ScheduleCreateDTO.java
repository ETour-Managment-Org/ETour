package com.example.demo.dto.tour;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCreateDTO {

    @NotNull(message = "startDate is required for every schedule")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    private Integer totalSeats;

    private Integer availableSeats;

    @Builder.Default
    private String status = "OPEN";
}
