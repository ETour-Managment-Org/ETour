package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostDTO {

    private Integer costId;
    private BigDecimal adultPrice;
    private BigDecimal singlePersonPrice;
    private BigDecimal extraPersonPrice;
    private BigDecimal childWithBedPrice;
    private BigDecimal childWithoutBedPrice;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Boolean isActive;
}
