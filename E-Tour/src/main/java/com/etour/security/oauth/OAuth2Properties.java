package com.etour.security.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class OAuth2Properties {
    @Value("${etour.oauth.enabled:false}")
    private boolean enabled;

    @Value("${etour.oauth.redirect-uri:http://localhost:5173/oauth/callback}")
    private String redirectUri;

    public static final String NOT_CONFIGURED = "not-configured";

    @Value("${spring.security.oauth2.client.registration.google.client-id:" + NOT_CONFIGURED + "}")
    private String googleClientId;

    public boolean isConfigured() {
        return enabled
                && googleClientId != null
                && !googleClientId.isBlank()
                && !NOT_CONFIGURED.equals(googleClientId.trim());
    }
}
