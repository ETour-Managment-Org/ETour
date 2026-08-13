package com.etour.dto.booking;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {
    @NotNull(message = "tourId is required")
    private Integer tourId;

    @NotNull(message = "scheduleId is required")
    private Integer scheduleId;

    @NotEmpty(message = "at least one passenger is required")
    @Valid
    @Builder.Default
    private List<PassengerDTO> passengers = new ArrayList<>();

    private String paymentMethod;

    @Size(max = 120, message = "contact name must be 120 characters or fewer")
    private String contactName;

    @Email(message = "contact email is not a valid address")
    @Size(max = 150, message = "contact email must be 150 characters or fewer")
    private String contactEmail;

    @Pattern(regexp = "^$|^[0-9]{10}$",
             message = "contact phone must be exactly 10 digits")
    private String contactPhone;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
