package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookMessage {

    private String from;

    private String id;

    private String timestamp;

    private String type;

    private WhatsAppWebhookText text;
}
