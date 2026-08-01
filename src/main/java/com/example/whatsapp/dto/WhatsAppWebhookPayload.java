package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Schema(description = "Meta WhatsApp webhook payload")
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookPayload {

    @Schema(description = "Webhook object type", example = "whatsapp_business_account")
    private String object;

    @Schema(description = "Webhook entries containing message changes")
    private List<WhatsAppWebhookEntry> entry;
}
