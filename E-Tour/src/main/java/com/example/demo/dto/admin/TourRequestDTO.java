package com.example.demo.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourRequestDTO {

    @NotBlank(message = "tourName is required")
    private String tourName;

    private String destination;
    private Integer days;
    private Integer nights;
    private String description;
    private Float price;
    private String location;

    private Integer categoryId;
    private Integer subCategoryId;

    private String stayAndMeals;
    private String addOns;
    private String passportAndVisa;
    private String weather;
    private String doAndDont;
}
