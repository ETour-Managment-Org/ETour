package com.etour.dto.booking;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationRequestDTO {
    @NotBlank(message = "a cancellation reason is required")
    private String reason;
}
