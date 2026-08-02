package com.whatsflow.conversation.api;


import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.conversation.domain.Conversation;
import com.whatsflow.conversation.service.ConversationService;
import com.whatsflow.message.domain.Message;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/conversations")
@Tag(name = "Conversations")
public class ConversationController {
    private final ConversationService service;
    public ConversationController(ConversationService service) { this.service = service; }

    @GetMapping public ApiResponse<Page<Conversation>> list(Pageable pageable) { return ApiResponse.ok(service.list(pageable)); }
    @GetMapping("/{id}/messages") public ApiResponse<Page<Message>> messages(@PathVariable UUID id, Pageable pageable) {
        return ApiResponse.ok(service.timeline(id, pageable));
    }
    @PostMapping("/{id}/assign") public ApiResponse<Conversation> assign(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(service.assign(id, UUID.fromString(body.get("agentUserId"))));
    }
}
