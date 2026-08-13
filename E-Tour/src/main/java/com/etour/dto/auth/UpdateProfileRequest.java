package com.etour.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    @NotBlank(message = "first name is required")
    private String firstName;

    private String lastName;

    @NotBlank(message = "email is required")
    @Email(message = "a valid email address is required")
    private String email;

    @Pattern(regexp = "^$|^[6-9][0-9]{9}$",
             message = "phone number must be 10 digits and start with 6, 7, 8 or 9")
    private String phoneNumber;

    private String address;

    private String city;

    private String gender;
}
