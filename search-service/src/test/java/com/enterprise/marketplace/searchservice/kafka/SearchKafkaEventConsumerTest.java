package com.enterprise.marketplace.searchservice.kafka;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.enterprise.marketplace.searchservice.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchKafkaEventConsumerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchKafkaEventConsumer consumer;

    @Test
    void shouldProcessSearchIndexEvent() {
        doNothing().when(searchService).processIndexEvent("{}", "search-index");
        consumer.onSearchIndex("{}");
        verify(searchService).processIndexEvent("{}", "search-index");
    }

    @Test
    void shouldProcessProductDeletedEvent() {
        doNothing().when(searchService).processIndexEvent("{}", "product-deleted");
        consumer.onProductDeleted("{}");
        verify(searchService).processIndexEvent("{}", "product-deleted");
    }
}
