package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookEntry {

    private String id;

    private List<WhatsAppWebhookChange> changes;
}
