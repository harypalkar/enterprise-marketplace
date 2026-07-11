package com.enterprise.marketplace.searchservice.constants;

public final class SearchKafkaTopics {

    public static final String SEARCH_INDEX = "search-index";
    public static final String PRODUCT_CREATED = "product-created";
    public static final String PRODUCT_UPDATED = "product-updated";
    public static final String PRODUCT_DELETED = "product-deleted";
    public static final String SEARCH_INDEXED = "search-indexed";
    public static final String SEARCH_FAILED = "search-failed";
    public static final String AUDIT_CREATED = "audit-created";

    private SearchKafkaTopics() {}
}
