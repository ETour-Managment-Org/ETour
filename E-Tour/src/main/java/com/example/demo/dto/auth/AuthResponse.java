package com.example.demo.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresInMs;

    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
}
