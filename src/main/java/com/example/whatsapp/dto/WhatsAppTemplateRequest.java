package com.example.whatsapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Request body for sending a WhatsApp template message")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppTemplateRequest {

    @NotBlank(message = "mobile is required")
    @Pattern(regexp = "^[0-9]+$", message = "mobile must contain digits only")
    @Schema(description = "Recipient mobile with country code", example = "917718986249")
    private String mobile;

    @NotBlank(message = "templateName is required")
    @Schema(description = "Approved template name", example = "altitude_welcome_promo")
    private String templateName;

    @Builder.Default
    @Schema(description = "Template language code", example = "en_US")
    private String languageCode = "en_US";

    /** Body variables in order: {{1}}, {{2}}, ... */
    @Builder.Default
    private List<String> bodyParameters = new ArrayList<>();
}
