package com.etour.dto.booking;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerDTO {
    @NotBlank(message = "full name is required for every passenger")
    private String fullName;

    @NotNull(message = "birth date is required - the fare band is derived from it")
    @Past(message = "birth date must be in the past")
    private LocalDate birthDate;

    private String gender;

    private String email;

    private String passportNumber;

    @Builder.Default
    private Boolean withBed = Boolean.TRUE;

    private String occupancy;
}
