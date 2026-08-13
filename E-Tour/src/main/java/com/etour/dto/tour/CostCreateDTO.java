package com.etour.dto.tour;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCreateDTO {
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
