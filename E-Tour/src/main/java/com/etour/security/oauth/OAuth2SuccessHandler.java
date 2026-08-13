package com.etour.security.oauth;

import java.io.IOException;
import java.time.LocalDateTime;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.etour.common.RoleName;
import com.etour.email.EmailService;
import com.etour.entities.Role;
import com.etour.entities.User;
import com.etour.repositories.RoleRepository;
import com.etour.repositories.UserRepository;
import com.etour.security.jwt.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final OAuth2Properties properties;

    public OAuth2SuccessHandler(UserRepository userRepository,
                                RoleRepository roleRepository,
                                JwtUtils jwtUtils,
                                EmailService emailService,
                                OAuth2Properties properties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
        this.properties = properties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = attr(oauthUser, "email");
        String name = attr(oauthUser, "name");
        String givenName = attr(oauthUser, "given_name");
        String familyName = attr(oauthUser, "family_name");
        String providerId = attr(oauthUser, "sub");

        if (email == null || email.isBlank()) {
            log.warn("Google returned no e-mail address, cannot sign the user in");
            redirect(request, response, null, "no_email");
            return;
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = createFromGoogle(email, name, givenName, familyName, providerId);
            log.info("Created account {} from Google sign-in", user.getUsername());
            emailService.sendWelcomeEmail(user);
        } else if (user.getProviderId() == null) {
            user.setAuthProvider("GOOGLE");
            user.setProviderId(providerId);
            userRepository.save(user);
            log.info("Linked Google sign-in to existing account {}", user.getUsername());
        }

        if (Boolean.FALSE.equals(user.getIsactive())) {
            redirect(request, response, null, "account_disabled");
            return;
        }

        String token = jwtUtils.generateTokenFromUsername(user.getUsername());
        redirect(request, response, token, null);
    }

    private User createFromGoogle(String email, String name,
                                  String givenName, String familyName, String providerId) {
        Role customerRole = roleRepository.findByRoleName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException(
                        "Role CUSTOMER is missing. Seed the role table."));

        return userRepository.save(User.builder()
                .username(uniqueUsername(email))
                .email(email)
                .firstName(givenName != null ? givenName : name)
                .lastName(familyName)

                .passwordHash(null)
                .authProvider("GOOGLE")
                .providerId(providerId)
                .role(customerRole)
                .isactive(Boolean.TRUE)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private String uniqueUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^A-Za-z0-9._-]", "");
        if (base.isBlank()) {
            base = "user";
        }
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          String token, String error) throws IOException {
        StringBuilder url = new StringBuilder(properties.getRedirectUri());
        url.append(token != null ? "?token=" : "?error=");
        url.append(URLEncoder.encode(token != null ? token : error, StandardCharsets.UTF_8));

        getRedirectStrategy().sendRedirect(request, response, url.toString());
    }

    private String attr(OAuth2User user, String key) {
        Object value = user.getAttribute(key);
        return value == null ? null : value.toString();
    }
}
