package com.example.whatsapp.controller;

import com.example.whatsapp.entity.MessageRecord;
import com.example.whatsapp.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations V1")
public class ConversationV1Controller {

    private final ConversationService conversationService;

    @GetMapping("/{mobile}")
    @Operation(summary = "Conversation timeline for a customer mobile")
    public ResponseEntity<List<MessageRecord>> timeline(@PathVariable String mobile) {
        return ResponseEntity.ok(conversationService.timeline(mobile));
    }
}
