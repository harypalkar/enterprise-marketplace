package com.enterprise.marketplace.reportservice.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class ReportEventBuffer {

    private static final int MAX_EVENTS = 100;

    private final ConcurrentLinkedDeque<Map<String, Object>> workflowEvents = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Map<String, Object>> productEvents = new ConcurrentLinkedDeque<>();
    private final AtomicInteger workflowCompletedCount = new AtomicInteger(0);
    private final AtomicInteger productCreatedCount = new AtomicInteger(0);

    public void recordWorkflowCompleted(Map<String, Object> event) {
        workflowCompletedCount.incrementAndGet();
        addBounded(workflowEvents, event);
    }

    public void recordProductCreated(Map<String, Object> event) {
        productCreatedCount.incrementAndGet();
        addBounded(productEvents, event);
    }

    public List<Map<String, Object>> getWorkflowEvents(int limit) {
        return tail(workflowEvents, limit);
    }

    public List<Map<String, Object>> getProductEvents(int limit) {
        return tail(productEvents, limit);
    }

    public int getWorkflowCompletedCount() {
        return workflowCompletedCount.get();
    }

    public int getProductCreatedCount() {
        return productCreatedCount.get();
    }

    private void addBounded(ConcurrentLinkedDeque<Map<String, Object>> deque, Map<String, Object> event) {
        deque.addFirst(Collections.unmodifiableMap(event));
        while (deque.size() > MAX_EVENTS) {
            deque.pollLast();
        }
    }

    private List<Map<String, Object>> tail(ConcurrentLinkedDeque<Map<String, Object>> deque, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        int count = 0;
        for (Map<String, Object> event : deque) {
            result.add(event);
            count++;
            if (count >= limit) {
                break;
            }
        }
        return result;
    }
}
