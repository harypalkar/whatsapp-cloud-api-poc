package com.example.whatsapp.controller;

import com.example.whatsapp.dto.WhatsAppRequest;
import com.example.whatsapp.service.WhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/send")
@RequiredArgsConstructor
@Tag(name = "WhatsApp", description = "WhatsApp Cloud API messaging")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @PostMapping
    @Operation(
            summary = "Send WhatsApp message",
            description = "Sends a text message to the given mobile number via WhatsApp Cloud API.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Message sent successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Success response",
                                    value = """
                                            {
                                              "messaging_product": "whatsapp",
                                              "contacts": [
                                                {
                                                  "input": "917506426501",
                                                  "wa_id": "917506426501"
                                                }
                                              ],
                                              "messages": [
                                                {
                                                  "id": "wamid.example"
                                                }
                                              ]
                                            }
                                            """))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "502", description = "WhatsApp Cloud API error")
    })
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestBody(
                    description = "Mobile number and message text",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = WhatsAppRequest.class),
                            examples = @ExampleObject(
                                    name = "Send message",
                                    summary = "POST /api/send",
                                    value = """
                                            {
                                              "mobile": "917506426501",
                                              "message": "Hello User"
                                            }
                                            """)))
            @Valid @org.springframework.web.bind.annotation.RequestBody WhatsAppRequest request) {
        return ResponseEntity.ok(whatsAppService.sendMessage(request));
    }

    @PostMapping("/template")
    @Operation(
            summary = "Send WhatsApp template message",
            description = "Sends an approved template (e.g. hello_world) for first-contact messages.")
    public ResponseEntity<Map<String, Object>> sendTemplateMessage(
            @Valid @org.springframework.web.bind.annotation.RequestBody
            com.example.whatsapp.dto.WhatsAppTemplateRequest request) {
        return ResponseEntity.ok(whatsAppService.sendTemplateMessage(request));
    }
}
