package com.etour.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourListDTO {
    private Integer tourId;
    private String tourName;
    private String destination;

    private String tourType;
    private Integer days;
    private Integer nights;

    private BigDecimal startingPrice;

    private String primaryImageUrl;

    private String durationLabel;

    private Double averageRating;

    private Long reviewCount;
}
