package com.etour.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etour.security.oauth.OAuth2Properties;

@RestController
@RequestMapping("/api/auth")
public class AuthProviderController {
    private final OAuth2Properties oAuth2Properties;

    public AuthProviderController(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
    }

    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> providers() {
        return new ResponseEntity<>(Map.of(
                "password", true,
                "google", oAuth2Properties.isConfigured()), HttpStatus.OK);
    }
}
