package com.example.demo.dto.admin;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    private Integer totalSeats;

    private Integer availableSeats;

    @Builder.Default
    private String status = "OPEN";
}
