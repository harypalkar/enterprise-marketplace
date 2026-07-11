package com.enterprise.marketplace.aiservice.redis;

import java.util.Optional;

public interface AiCachePort {

    void cachePromptTemplate(String templateCode, String json);

    Optional<String> getPromptTemplate(String templateCode);

    void cacheProductSnapshot(String productId, String json);

    Optional<String> getProductSnapshot(String productId);

    void setFeatureEnabled(boolean enabled);

    Optional<Boolean> getFeatureEnabled();
}
