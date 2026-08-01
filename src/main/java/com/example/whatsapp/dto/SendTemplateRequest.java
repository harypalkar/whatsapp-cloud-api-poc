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
@Schema(description = "Send approved marketing template (cold outreach)")
public class SendTemplateRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "mobile must be digits only with country code")
    @Schema(example = "917506426501", description = "E.164 digits without +")
    private String mobile;

    @Schema(example = "Harish", description = "Optional when template has no body variables")
    private String customerName;

    @Schema(example = "WELCOME100", description = "Optional when template has no body variables")
    private String promoCode;
}
