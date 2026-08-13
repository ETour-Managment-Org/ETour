package com.etour.dto.review;

import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private Integer reviewId;
    private Integer rating;
    private String reviewTitle;
    private String reviewDescription;
    private LocalDate reviewDate;
    private String verificationStatus;

    private Integer tourId;
    private String tourName;

    private Integer bookingId;

    private String customerName;
}
