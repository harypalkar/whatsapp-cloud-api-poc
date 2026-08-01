package com.example.whatsapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request body for sending a WhatsApp text message")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppRequest {

    @NotBlank(message = "mobile is required")
    @Pattern(regexp = "^[0-9]+$", message = "mobile must contain digits only")
    @Schema(
            description = "Recipient mobile number with country code (no + prefix)",
            example = "917506426501")
    private String mobile;

    @NotBlank(message = "message is required")
    @Schema(description = "Text message to send", example = "Hello User")
    private String message;
}
