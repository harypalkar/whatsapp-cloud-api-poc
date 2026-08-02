package com.whatsflow.rag.service;


import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {
    public List<String> chunk(String text, int size, int overlap) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isBlank()) return out;
        int i = 0;
        while (i < text.length()) {
            int end = Math.min(text.length(), i + size);
            out.add(text.substring(i, end));
            if (end == text.length()) break;
            i = Math.max(0, end - overlap);
        }
        return out;
    }
}
