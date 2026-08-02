package com.whatsflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "whatsflow")
public class WhatsFlowProperties {

    private EncryptionProperties encryption = new EncryptionProperties();
    private CorsProperties cors = new CorsProperties();
    private MetaProperties meta = new MetaProperties();

    @Getter
    @Setter
    public static class EncryptionProperties {
        private String aesKey;
    }

    @Getter
    @Setter
    public static class CorsProperties {
        private String[] allowedOrigins = {"http://localhost:4200"};
        private String[] allowedMethods = {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"};
        private String[] allowedHeaders = {"*"};
        private boolean allowCredentials = true;
    }

    @Getter
    @Setter
    public static class MetaProperties {
        private String appId = "";
        private String appSecret = "";
        private String configId = "";
        private String graphApiVersion = "v23.0";
        private String redirectUri = "http://localhost:4200/meta/callback";
    }
}
