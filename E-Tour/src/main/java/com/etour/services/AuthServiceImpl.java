package com.etour.services;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.common.RoleName;
import com.etour.dto.auth.AuthResponse;
import com.etour.dto.auth.ChangePasswordRequest;
import com.etour.dto.auth.LoginRequest;
import com.etour.dto.auth.RegisterRequest;
import com.etour.common.Gender;
import com.etour.email.EmailService;
import com.etour.dto.auth.UpdateProfileRequest;
import com.etour.dto.auth.UserProfileDTO;
import com.etour.entities.Role;
import com.etour.entities.User;
import com.etour.exceptions.ResourceNotFoundException;
import com.etour.repositories.RoleRepository;
import com.etour.repositories.UserRepository;
import com.etour.security.jwt.JwtUtils;
import com.etour.security.services.UserDetailsImpl;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "An account already exists for " + request.getEmail());
        }

        Role customerRole = roleRepository.findByRoleName(RoleName.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role CUSTOMER is missing. Seed the role table (see data.sql)."));

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .gender(Gender.normalise(request.getGender()))
                .role(customerRole)
                .isactive(Boolean.TRUE)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        emailService.sendWelcomeEmail(saved);

        String token = jwtUtils.generateTokenFromUsername(saved.getUsername());

        return buildResponse(token, saved, RoleName.CUSTOMER);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();

        String token = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getUserId()));

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : RoleName.CUSTOMER;

        return buildResponse(token, user, roleName);
    }

    private AuthResponse buildResponse(String token, User user, String roleName) {
        String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                         + (user.getLastName() != null ? user.getLastName() : "")).trim();

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtExpirationMs)
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(fullName.isEmpty() ? user.getUsername() : fullName)
                .role(roleName)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (first + " " + last).trim();
        if (fullName.isEmpty()) {
            fullName = user.getUsername();
        }

        return UserProfileDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(fullName)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .city(user.getCity())
                .gender(user.getGender())
                .role(user.getRole() != null ? user.getRole().getRoleName() : null)
                .authProvider(user.getAuthProvider())
                .build();
    }

    @Override
    @Transactional
    public UserProfileDTO updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        String newEmail = request.getEmail();
        if (newEmail != null && !newEmail.equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("An account already exists for " + newEmail);
        }

        if (!Gender.isValid(request.getGender())) {
            throw new IllegalArgumentException(
                    "gender must be one of " + Gender.ALL + " or left empty");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(newEmail);
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setGender(Gender.normalise(request.getGender()));

        userRepository.save(user);

        return getProfile(user.getUsername());
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new IllegalStateException(
                    "This account signs in with Google and has no password to change.");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Your current password is not correct");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "The new password must be different from the current one");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        User saved = userRepository.save(user);

        emailService.sendPasswordChangedEmail(saved, false);

        log.info("Password changed for {}", username);
    }

    @Override
    @Transactional
    public void resetPassword(Integer userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("new password must be at least 8 characters");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        User saved = userRepository.save(user);

        emailService.sendPasswordChangedEmail(saved, true);

        log.info("Password reset by an administrator for {}", user.getUsername());
    }
}
