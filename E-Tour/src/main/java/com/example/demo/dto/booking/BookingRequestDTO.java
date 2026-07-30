package com.example.demo.dto.booking;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {

    @NotNull(message = "tourId is required")
    private Integer tourId;

    @NotNull(message = "scheduleId is required")
    private Integer scheduleId;

    @NotEmpty(message = "at least one passenger is required")
    @Valid
    @Builder.Default
    private List<PassengerDTO> passengers = new ArrayList<>();

    private String paymentMethod;
}
