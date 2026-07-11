package com.enterprise.marketplace.searchservice.constants;

public final class SearchCacheKeys {

    public static final String SEARCH_PREFIX = "search:query:";

    private SearchCacheKeys() {}

    public static String searchQueryKey(String hash) {
        return SEARCH_PREFIX + hash;
    }
}
