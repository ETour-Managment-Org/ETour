package com.etour.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostRequestDTO {
    @NotNull(message = "tourId is required")
    private Integer tourId;

    private BigDecimal adultPrice;
    private BigDecimal singlePersonPrice;
    private BigDecimal extraPersonPrice;
    private BigDecimal childWithBedPrice;
    private BigDecimal childWithoutBedPrice;

    private LocalDate validFrom;

    private LocalDate validTo;

    @Builder.Default
    private Boolean isActive = Boolean.TRUE;
}
