package com.etour.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "etour.mail")
@Data
public class EmailProperties {
    private boolean enabled = false;

    private String from = "noreply@etour.co.in";
    private String fromName = "e-Tour by IndiaTour";
    private String supportEmail = "support@etour.co.in";
    private String supportPhone = "+91 98200 11223";

    private String overrideRecipient = "";

    public boolean hasOverride() {
        return overrideRecipient != null && !overrideRecipient.isBlank();
    }
}
