package com.etour.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerResponseDTO {
    private Integer paxId;
    private String fullName;
    private LocalDate birthDate;

    private Integer ageAtDeparture;

    private String paxType;

    private String paxTypeLabel;

    private BigDecimal paxAmount;
    private String gender;
    private String passportNumber;
    private String email;
}
