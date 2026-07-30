package com.example.demo.dto.tour;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

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

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validTo;

    @Builder.Default
    private Boolean isActive = Boolean.TRUE;
}
