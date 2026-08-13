package com.etour.dto.tour;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourImageCreateDTO {
    private String source;

    private String imageTitle;

    @Builder.Default
    private Boolean isPrimary = Boolean.FALSE;
}
