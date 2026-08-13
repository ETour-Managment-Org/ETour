package com.etour.services;

import com.etour.dto.auth.AuthResponse;
import com.etour.dto.auth.ChangePasswordRequest;
import com.etour.dto.auth.LoginRequest;
import com.etour.dto.auth.RegisterRequest;
import com.etour.dto.auth.UpdateProfileRequest;
import com.etour.dto.auth.UserProfileDTO;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileDTO getProfile(String username);

    UserProfileDTO updateProfile(String username, UpdateProfileRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    void resetPassword(Integer userId, String newPassword);
}
