package com.whatsflow.ai.provider;


import com.whatsflow.ai.config.AIProperties;
import com.whatsflow.ai.spi.*;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class OllamaProvider implements AIProvider {
    private final AIProperties props;
    public OllamaProvider(AIProperties props) { this.props = props; }
    @Override public String id() { return "ollama"; }
    @Override public boolean supports(AICapability capability) { return true; }
    @Override public AIChatResponse chat(AIChatRequest request) {
        long t0 = System.currentTimeMillis();
        String last = request.messages() == null || request.messages().isEmpty() ? "" :
                request.messages().get(request.messages().size() - 1).content();
        String model = request.model() != null ? request.model() : props.getDefaultModel();
        return new AIChatResponse(model, "[" + id() + "] " + last, 0, 0, System.currentTimeMillis() - t0);
    }
    @Override public List<float[]> embed(AIEmbedRequest request) { return Collections.emptyList(); }
}
