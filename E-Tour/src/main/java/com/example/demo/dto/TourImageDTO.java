package com.example.demo.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourImageDTO {

    private Integer imageId;
    private String source;
    private String imageTitle;
    private Boolean isPrimary;
    private LocalDateTime uploadDate;
}
