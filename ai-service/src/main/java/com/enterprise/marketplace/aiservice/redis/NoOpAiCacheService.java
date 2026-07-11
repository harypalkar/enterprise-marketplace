package com.enterprise.marketplace.aiservice.redis;

import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "marketplace.redis", name = "enabled", havingValue = "false")
public class NoOpAiCacheService implements AiCachePort {

    @Override
    public void cachePromptTemplate(String templateCode, String json) {}

    @Override
    public Optional<String> getPromptTemplate(String templateCode) {
        return Optional.empty();
    }

    @Override
    public void cacheProductSnapshot(String productId, String json) {}

    @Override
    public Optional<String> getProductSnapshot(String productId) {
        return Optional.empty();
    }

    @Override
    public void setFeatureEnabled(boolean enabled) {}

    @Override
    public Optional<Boolean> getFeatureEnabled() {
        return Optional.empty();
    }
}
