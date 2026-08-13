package com.etour.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid address")
    private String email;

    private String firstName;
    private String lastName;

    @Pattern(regexp = "^$|^[6-9][0-9]{9}$",
             message = "phone number must be 10 digits and start with 6, 7, 8 or 9")
    private String phoneNumber;
    private String address;

    private String gender;
    private String city;
    private String state;
    private String pinCode;
}
