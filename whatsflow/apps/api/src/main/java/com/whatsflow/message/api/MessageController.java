package com.whatsflow.message.api;


import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.message.domain.Message;
import com.whatsflow.message.service.MessageSendService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/messages")
@Tag(name = "Messages")
public class MessageController {
    private final MessageSendService service;
    public MessageController(MessageSendService service) { this.service = service; }

    @PostMapping("/text")
    public ApiResponse<Message> text(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(service.sendText(UUID.fromString(body.get("customerId")), body.get("body")));
    }

    @PostMapping("/template")
    public ApiResponse<Message> template(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> params = (List<String>) body.getOrDefault("bodyParameters", List.of());
        return ApiResponse.ok(service.sendTemplate(
                UUID.fromString(String.valueOf(body.get("customerId"))),
                String.valueOf(body.get("templateName")),
                body.get("language") == null ? "en" : String.valueOf(body.get("language")),
                params));
    }
}
