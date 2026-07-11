package com.enterprise.marketplace.workflowservice.config;

import com.enterprise.marketplace.workflowservice.redis.WorkflowCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowStartupConfig {

    private final WorkflowCachePort cachePort;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        cachePort.refreshTransitionRules();
        log.info("Workflow transition rules loaded into cache");
    }
}
