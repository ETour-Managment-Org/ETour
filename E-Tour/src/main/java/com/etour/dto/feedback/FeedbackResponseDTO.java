package com.etour.dto.feedback;

import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponseDTO {
    private Integer feedbackId;
    private String name;
    private String email;
    private String category;
    private Integer rating;
    private String message;
    private String status;
    private String pageUrl;
    private LocalDateTime createdAt;

    private Integer userId;
    private String username;

    private boolean fromRegisteredUser;

    private boolean published;
}
