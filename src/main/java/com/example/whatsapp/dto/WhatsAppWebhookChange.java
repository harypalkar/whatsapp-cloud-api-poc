package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookChange {

    private String field;

    private WhatsAppWebhookValue value;
}
