package com.enterprise.marketplace.workflowservice.redis;

import com.enterprise.marketplace.workflowservice.constants.WorkflowCacheKeys;
import com.enterprise.marketplace.workflowservice.dto.WorkflowResponse;
import com.enterprise.marketplace.workflowservice.entity.WorkflowTransitionEntity;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.workflowservice.repository.WorkflowTransitionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WorkflowRedisCacheService implements WorkflowCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final WorkflowTransitionRepository transitionRepository;

    @Value("${marketplace.redis.workflow-cache-ttl-seconds:3600}")
    private long cacheTtlSeconds;

    @Override
    public void cacheWorkflow(WorkflowResponse response) {
        try {
            String key = WorkflowCacheKeys.workflowKey(response.getId());
            redisTemplate
                    .opsForValue()
                    .set(key, objectMapper.writeValueAsString(response), Duration.ofSeconds(cacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache workflow id={}", response.getId(), ex);
        }
    }

    @Override
    public void evictWorkflow(UUID workflowId) {
        redisTemplate.delete(WorkflowCacheKeys.workflowKey(workflowId));
    }

    @Override
    public void refreshTransitionRules() {
        List<WorkflowTransitionEntity> transitions = transitionRepository.findByActiveTrue();
        Set<String> rules = new HashSet<>();
        for (WorkflowTransitionEntity transition : transitions) {
            rules.add(ruleKey(transition.getFromStatus(), transition.getToStatus()));
        }
        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            WorkflowCacheKeys.TRANSITION_RULES,
                            objectMapper.writeValueAsString(rules),
                            Duration.ofSeconds(cacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache transition rules", ex);
        }
    }

    @Override
    public boolean isTransitionAllowed(WorkflowStatus fromStatus, WorkflowStatus toStatus) {
        try {
            String cached = redisTemplate.opsForValue().get(WorkflowCacheKeys.TRANSITION_RULES);
            if (cached == null || cached.isBlank()) {
                return false;
            }
            Set<String> rules = objectMapper.readValue(cached, new TypeReference<>() {});
            return rules.contains(ruleKey(fromStatus, toStatus));
        } catch (Exception ex) {
            log.debug("Transition rule cache miss or parse error", ex);
            return false;
        }
    }

    private String ruleKey(WorkflowStatus from, WorkflowStatus to) {
        return from.name() + "->" + to.name();
    }
}
