package com.etour.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.etour.common.RoleName;
import com.etour.security.jwt.AuthEntryPointJwt;
import com.etour.security.oauth.OAuth2Properties;
import com.etour.security.oauth.OAuth2SuccessHandler;
import com.etour.security.JwtAccessDeniedHandler;
import com.etour.security.jwt.AuthTokenFilter;
import com.etour.security.services.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final List<String> ALLOWED_ORIGINS =
            List.of("http://localhost:5173", "http://localhost:3000");

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthTokenFilter authTokenFilter;
    private final AuthEntryPointJwt authEntryPointJwt;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2Properties oAuth2Properties;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          AuthTokenFilter authTokenFilter,
                          AuthEntryPointJwt authEntryPointJwt,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          OAuth2Properties oAuth2Properties) {
        this.userDetailsService = userDetailsService;
        this.authTokenFilter = authTokenFilter;
        this.authEntryPointJwt = authEntryPointJwt;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authEntryPointJwt)
                    .accessDeniedHandler(jwtAccessDeniedHandler))
            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/providers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tours/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/tour/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                .requestMatchers(HttpMethod.POST,   "/api/tours/**").hasRole(RoleName.ADMIN)
                .requestMatchers(HttpMethod.PUT,    "/api/tours/**").hasRole(RoleName.ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/tours/**").hasRole(RoleName.ADMIN)

                .requestMatchers(HttpMethod.GET, "/images/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/api/feedback").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/feedback/categories").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/feedback/published").permitAll()

                .requestMatchers("/error").permitAll()

                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                .requestMatchers(HttpMethod.GET,
                        "/", "/index.html", "/favicon.ico", "/vite.svg",
                        "/assets/**", "/*.js", "/*.css", "/*.png", "/*.svg", "/*.ico").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/home", "/categories/**", "/tours/**", "/search", "/feedback",
                        "/login", "/register", "/oauth/callback",
                        "/booking/**", "/dashboard", "/admin/**", "/forbidden").permitAll()

                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole(RoleName.ADMIN)

                .requestMatchers("/api/admin/**").hasRole(RoleName.ADMIN)

                .requestMatchers(HttpMethod.GET,  "/api/payments/config").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/payments/order").authenticated()

                .requestMatchers("/api/bookings/**").authenticated()
                .requestMatchers("/api/auth/me", "/api/auth/change-password").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/reviews/**").authenticated()
                .requestMatchers(HttpMethod.PUT,    "/api/reviews/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated()
                .requestMatchers("/api/reviews/my", "/api/reviews/reviewable").authenticated()

                .anyRequest().authenticated()
            );

        if (oAuth2Properties.isConfigured()) {
            http.oauth2Login(oauth -> oauth
                    .loginPage("/login")
                    .successHandler(oAuth2SuccessHandler));
        }

        http.authenticationProvider(daoAuthenticationProvider());
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(daoAuthenticationProvider());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(ALLOWED_ORIGINS);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
