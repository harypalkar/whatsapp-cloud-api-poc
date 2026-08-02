package com.whatsflow.ai.service;


import com.whatsflow.ai.config.AIProperties;
import com.whatsflow.ai.spi.AICapability;
import com.whatsflow.ai.spi.AIProvider;
import com.whatsflow.exception.BusinessException;
import com.whatsflow.exception.ErrorCode;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AIProviderRouter {
    private final Map<String, AIProvider> providers;
    private final AIProperties properties;

    public AIProviderRouter(List<AIProvider> list, AIProperties properties) {
        this.providers = list.stream().collect(Collectors.toMap(p -> p.id().toLowerCase(Locale.ROOT), Function.identity(), (a,b)->a));
        this.properties = properties;
    }

    public AIProvider resolve(String providerId) {
        String id = (providerId == null || providerId.isBlank()) ? properties.getDefaultProvider() : providerId;
        AIProvider p = providers.get(id.toLowerCase(Locale.ROOT));
        if (p == null) p = providers.get(properties.getFallbackProvider().toLowerCase(Locale.ROOT));
        if (p == null) throw new BusinessException(ErrorCode.BUSINESS_RULE, "No AI provider: " + id);
        return p;
    }

    public AIProvider resolveFor(AICapability capability, String providerId) {
        AIProvider p = resolve(providerId);
        return p.supports(capability) ? p : resolve(properties.getFallbackProvider());
    }
}
