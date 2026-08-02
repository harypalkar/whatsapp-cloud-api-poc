package com.whatsflow.rag.api;


import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.rag.service.RagQueryService;
import com.whatsflow.rag.service.TextChunker;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/rag")
@Tag(name = "RAG")
public class RagController {
    private final RagQueryService rag;
    private final TextChunker chunker;
    public RagController(RagQueryService rag, TextChunker chunker) { this.rag = rag; this.chunker = chunker; }

    @PostMapping("/ask")
    public ApiResponse<Map<String, Object>> ask(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> chunks = (List<String>) body.getOrDefault("chunks", List.of());
        if (chunks.isEmpty() && body.get("documentText") instanceof String doc) {
            chunks = chunker.chunk(doc, 800, 120);
        }
        return ApiResponse.ok(rag.ask(String.valueOf(body.get("question")), chunks));
    }
}
