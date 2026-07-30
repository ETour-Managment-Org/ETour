package com.tourism.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDTO {
    private Integer tourId;
    private String tourName;
    private String destination;
    private Integer days;
    private Integer nights;
    private String description;
    private Double price;
    private String location;
    
    // Category mapping details
    private Integer categoryId;
    private String categoryName;
}