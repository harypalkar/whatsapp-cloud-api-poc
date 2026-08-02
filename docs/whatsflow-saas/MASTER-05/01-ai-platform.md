# MASTER-05 — AI Platform Architecture

## 1. Provider SPI

```java
public interface AIProvider {
    String id(); // openai | azure-openai | gemini | claude | ollama | openrouter
    boolean supports(AICapability capability);
    AIChatResponse chat(AIChatRequest request);
    List<float[]> embed(AIEmbedRequest request);
    AIModerationResult moderate(String text);
}
```

Runtime selection: `whatsflow.ai.provider` + per-tenant override in `ai_tenant_settings`.

## 2. Capabilities

| Feature | Capability enum | Typical model |
|---|---|---|
| AI Chatbot | CHAT | gpt-4.1 / gemini-2.0 / claude-sonnet |
| Knowledge Q&A | RAG_CHAT | chat + embeddings |
| Conversation Summary | SUMMARIZE | chat |
| Reply Suggestions | SUGGEST | chat |
| Intent Detection | INTENT | chat / classify |
| Sentiment | SENTIMENT | chat |
| Language Detect | LANGUAGE | chat |
| Translation | TRANSLATE | chat |
| FAQ | FAQ | RAG |
| Document Chat | DOC_CHAT | RAG |
| Product Recommendation | RECOMMEND | chat + CRM context |
| Lead Qualification | LEAD_SCORE | chat |
| Campaign Writer | COPYWRITE | chat |
| Image Caption | CAPTION | vision/chat |
| Report Generator | REPORT | chat |

## 3. Safety

- Prompt injection filters on user content  
- PII redaction before external LLM (optional)  
- Tenant API keys encrypted (AES-GCM)  
- Token/cost metering per company  
- Audit every AI invocation  

## 4. Switching

```yaml
whatsflow:
  ai:
    default-provider: openai
    fallback-provider: ollama
    openai:
      api-key: ${OPENAI_API_KEY:}
      base-url: https://api.openai.com/v1
    gemini:
      api-key: ${GEMINI_API_KEY:}
    claude:
      api-key: ${ANTHROPIC_API_KEY:}
    ollama:
      base-url: http://localhost:11434
    openrouter:
      api-key: ${OPENROUTER_API_KEY:}
```
