package com.etour.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDTO {
    private Integer scheduleId;
    private LocalDate startDate;
    private Integer availableSeats;
    private Integer totalSeats;
    private String status;
}
