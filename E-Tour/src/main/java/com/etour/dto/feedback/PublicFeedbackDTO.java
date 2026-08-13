package com.etour.dto.feedback;

import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicFeedbackDTO {
    private Integer feedbackId;

    private String name;

    private String category;

    private Integer rating;

    private String message;

    private LocalDate submittedOn;

    private boolean fromRegisteredUser;
}
