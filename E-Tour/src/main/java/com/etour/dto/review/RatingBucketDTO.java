package com.etour.dto.review;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingBucketDTO {
    private Integer stars;

    private Long count;

    private Double percentage;
}
