package com.whatsflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "whatsapp")
public class WhatsAppProperties {

    /** mock | meta */
    private String provider = "mock";
    private String accessToken = "";
    private String phoneNumberId = "";
    private String businessAccountId = "";
    private String verifyToken = "AltitudeLabs@2026";
    private String apiVersion = "v23.0";
    private String graphBaseUrl = "https://graph.facebook.com";
    private int webhookVerifyTimeoutSeconds = 30;
}
