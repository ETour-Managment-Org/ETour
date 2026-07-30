package com.tourism.demo.dto.request;



import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostRequest {

    private Integer tourId;

    private BigDecimal adultPrice;

    private BigDecimal singlePersonPrice;

    private BigDecimal extraPersonPrice;

    private BigDecimal childWithBedPrice;

    private BigDecimal childWithoutBedPrice;

    private LocalDate validFrom;

    private LocalDate validTo;

    private Boolean isActive;
}