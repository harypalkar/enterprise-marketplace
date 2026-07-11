package com.enterprise.marketplace.aiservice.constants;

public final class AiKafkaTopics {

    public static final String PRODUCT_CREATED = "product-created";
    public static final String PRODUCT_UPDATED = "product-updated";
    public static final String SEARCH_INDEX = "search-index";
    public static final String ADMIN_FEATURE_TOGGLED = "admin-feature-toggled";

    public static final String AI_DESCRIPTION_GENERATED = "ai-description-generated";
    public static final String AI_CHAT_COMPLETED = "ai-chat-completed";

    private AiKafkaTopics() {}
}
