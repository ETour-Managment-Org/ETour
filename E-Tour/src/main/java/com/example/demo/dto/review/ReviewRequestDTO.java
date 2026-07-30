package com.example.demo.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDTO {

    @NotNull(message = "bookingId is required - reviews are only allowed on tours you have travelled")
    private Integer bookingId;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;

    @Size(max = 150, message = "title must be 150 characters or fewer")
    private String reviewTitle;

    @Size(max = 1000, message = "comments must be 1000 characters or fewer")
    private String reviewDescription;
}
