package com.enterprise.marketplace.workflowservice.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.workflowservice.constants.WorkflowCacheKeys;
import com.enterprise.marketplace.workflowservice.dto.WorkflowResponse;
import com.enterprise.marketplace.workflowservice.entity.WorkflowTransitionEntity;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.workflowservice.repository.WorkflowTransitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WorkflowRedisCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private WorkflowTransitionRepository transitionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WorkflowRedisCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new WorkflowRedisCacheService(redisTemplate, objectMapper, transitionRepository);
        ReflectionTestUtils.setField(cacheService, "cacheTtlSeconds", 3600L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldCacheWorkflowResponse() throws Exception {
        WorkflowResponse response = WorkflowResponse.builder()
                .id(UUID.randomUUID())
                .requestId("cache-req")
                .status(WorkflowStatus.INITIAL)
                .build();

        cacheService.cacheWorkflow(response);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), anyString(), org.mockito.ArgumentMatchers.any());
        assertThat(keyCaptor.getValue()).isEqualTo(WorkflowCacheKeys.workflowKey(response.getId()));
    }

    @Test
    void shouldRefreshTransitionRules() throws Exception {
        WorkflowTransitionEntity transition = new WorkflowTransitionEntity();
        transition.setFromStatus(WorkflowStatus.INITIAL);
        transition.setToStatus(WorkflowStatus.RECEIVED);
        transition.setActive(true);
        transition.setCreatedAt(Instant.now());
        transition.setUpdatedAt(Instant.now());

        when(transitionRepository.findByActiveTrue()).thenReturn(List.of(transition));

        cacheService.refreshTransitionRules();

        verify(valueOperations).set(org.mockito.ArgumentMatchers.eq(WorkflowCacheKeys.TRANSITION_RULES), anyString(), org.mockito.ArgumentMatchers.any());
    }
}
