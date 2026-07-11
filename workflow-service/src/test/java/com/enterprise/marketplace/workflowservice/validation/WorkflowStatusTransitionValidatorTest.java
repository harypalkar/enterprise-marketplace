package com.enterprise.marketplace.workflowservice.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.workflowservice.redis.WorkflowCachePort;
import com.enterprise.marketplace.workflowservice.repository.WorkflowRepository;
import com.enterprise.marketplace.workflowservice.repository.WorkflowTransitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowStatusTransitionValidatorTest {

    @Mock
    private WorkflowTransitionRepository transitionRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowCachePort cachePort;

    @InjectMocks
    private WorkflowStatusTransitionValidator validator;

    @Test
    void shouldRejectSameStatusTransition() {
        assertThatThrownBy(() -> validator.validateTransition(WorkflowStatus.RECEIVED, WorkflowStatus.RECEIVED))
                .isInstanceOf(MarketplaceException.class)
                .hasMessageContaining("already in status");
    }

    @Test
    void shouldRejectDisallowedTransition() {
        when(cachePort.isTransitionAllowed(WorkflowStatus.INITIAL, WorkflowStatus.COMPLETED)).thenReturn(false);
        when(transitionRepository.existsByFromStatusAndToStatusAndActiveTrue(
                        WorkflowStatus.INITIAL, WorkflowStatus.COMPLETED))
                .thenReturn(false);

        assertThatThrownBy(() -> validator.validateTransition(WorkflowStatus.INITIAL, WorkflowStatus.COMPLETED))
                .isInstanceOf(MarketplaceException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void shouldAllowValidTransitionFromDatabase() {
        when(cachePort.isTransitionAllowed(WorkflowStatus.INITIAL, WorkflowStatus.RECEIVED)).thenReturn(false);
        when(transitionRepository.existsByFromStatusAndToStatusAndActiveTrue(
                        WorkflowStatus.INITIAL, WorkflowStatus.RECEIVED))
                .thenReturn(true);

        validator.validateTransition(WorkflowStatus.INITIAL, WorkflowStatus.RECEIVED);
    }
}
