package com.etour.dto.feedback;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequestDTO {
    @Size(max = 120, message = "name is too long")
    private String name;

    @Email(message = "a valid email address is required")
    @Size(max = 120)
    private String email;

    @NotBlank(message = "please choose what your feedback is about")
    private String category;

    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;

    @NotBlank(message = "please tell us what you think")
    @Size(max = 2000, message = "please keep it under 2000 characters")
    private String message;

    @Size(max = 255)
    private String pageUrl;
}
