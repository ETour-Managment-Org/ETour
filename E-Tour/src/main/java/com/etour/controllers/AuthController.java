package com.etour.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etour.dto.auth.AuthResponse;
import com.etour.dto.auth.ChangePasswordRequest;
import com.etour.dto.auth.LoginRequest;
import com.etour.dto.auth.RegisterRequest;
import com.etour.dto.auth.UpdateProfileRequest;
import com.etour.dto.auth.UserProfileDTO;
import com.etour.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> me(Authentication authentication) {
        return new ResponseEntity<>(
                authService.getProfile(authentication.getName()), HttpStatus.OK);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateMe(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        return new ResponseEntity<>(
                authService.updateProfile(authentication.getName(), request), HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<java.util.Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        authService.changePassword(authentication.getName(), request);

        return new ResponseEntity<>(
                java.util.Map.of("message", "Your password has been changed."), HttpStatus.OK);
    }
}
