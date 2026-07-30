package com.tourism.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourRequestDTO {
    private String tourName;
    private String destination;
    private Integer days;
    private Integer nights;
    private String description;
    private Double price;
    private String location;
    private Integer categoryId;
}