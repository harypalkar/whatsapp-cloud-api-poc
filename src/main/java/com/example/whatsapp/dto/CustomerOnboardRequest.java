package com.example.whatsapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Onboard a new Altitude Labs WhatsApp customer")
public class CustomerOnboardRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9]{10,15}$", message = "mobile must include country code digits only")
    @Schema(example = "917718986249", description = "E.164 digits without +")
    private String mobile;

    @Schema(example = "Amit")
    private String name;

    @Builder.Default
    @Schema(example = "WELCOME100")
    private String promoCode = "WELCOME100";

    @Builder.Default
    @Schema(example = "true")
    private Boolean sendWelcome = true;

    /**
     * template = cold outreach (any number, no prior Hi needed) — DEFAULT
     * cta/text/buttons ignored unless allowSessionMessage=true (24h window only)
     */
    @Builder.Default
    @Schema(example = "template", description = "template | cta | text | buttons")
    private String messageStyle = "template";

    /**
     * When false (default), onboarding always sends the marketing template so any mobile can be reached
     * without the customer saying Hi first.
     */
    @Builder.Default
    @Schema(example = "false")
    private Boolean allowSessionMessage = false;

    @Schema(example = "altitude_welcome_promo")
    private String templateName;

    @Schema(example = "en")
    private String languageCode;
}
