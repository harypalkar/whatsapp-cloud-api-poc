package com.example.whatsapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "whatsapp")
@Getter
@Setter
public class WhatsAppProperties {

    /** Maps to whatsapp.access-token */
    private String accessToken;

    /** Legacy alias bound from WHATSAPP_TOKEN / whatsapp.token */
    private String token;

    /** Maps to whatsapp.phone-number-id */
    private String phoneNumberId;

    /** Maps to whatsapp.business-account-id */
    private String businessAccountId;

    private String displayPhoneNumber = "919512618333";

    /** Graph API version, e.g. v23.0 */
    private String apiVersion = "v23.0";

    private String autoReplyMessage = "Thank you for contacting Altitude Labs.";

    /** Maps to whatsapp.template-name (dev default until marketing template approved) */
    private String templateName = "3p_direct_integration_test_template";

    /** Maps to whatsapp.template-language */
    private String templateLanguage = "en";

    /**
     * How many body variables the Meta template expects.
     * 0 = fixed body (as in current altitude_welcome_promo preview: Customer / WELCOME100 hardcoded).
     * 2 = {{1}} name, {{2}} promo code.
     */
    private int templateBodyParamCount = 0;

    /** Backward-compatible aliases */
    private String marketingTemplateName;
    private String marketingTemplateLanguage;

    private Webhook webhook = new Webhook();

    public String getAccessToken() {
        if (StringUtils.hasText(accessToken)) {
            return accessToken;
        }
        return token;
    }

    /** Legacy accessor used across services. */
    public String getToken() {
        return getAccessToken();
    }

    public String getResolvedTemplateName() {
        if (StringUtils.hasText(templateName)) {
            return templateName;
        }
        return StringUtils.hasText(marketingTemplateName)
                ? marketingTemplateName
                : "3p_direct_integration_test_template";
    }

    public String getResolvedTemplateLanguage() {
        if (StringUtils.hasText(templateLanguage)) {
            return templateLanguage;
        }
        return StringUtils.hasText(marketingTemplateLanguage) ? marketingTemplateLanguage : "en";
    }

    public String getMarketingTemplateName() {
        return getResolvedTemplateName();
    }

    public String getMarketingTemplateLanguage() {
        return getResolvedTemplateLanguage();
    }

    public String getVerifyToken() {
        return webhook != null ? webhook.getVerifyToken() : null;
    }

    public void setVerifyToken(String verifyToken) {
        if (webhook == null) {
            webhook = new Webhook();
        }
        webhook.setVerifyToken(verifyToken);
    }

    @Getter
    @Setter
    public static class Webhook {
        /** Maps to whatsapp.verify-token or whatsapp.webhook.verify-token */
        private String verifyToken = "AltitudeLabs@2026";
    }
}
