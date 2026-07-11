package com.enterprise.marketplace.workflowservice.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.workflowservice.entity.WorkflowEntity;
import com.enterprise.marketplace.workflowservice.entity.WorkflowHistoryEntity;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import org.junit.jupiter.api.Test;

class WorkflowEngineTest {

    private final WorkflowEngine engine = new WorkflowEngine();

    @Test
    void shouldApplyTransitionAndUpdatePreviousStatus() {
        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setStatus(WorkflowStatus.INITIAL);

        engine.applyTransition(workflow, WorkflowStatus.RECEIVED, "Intake complete", "tester");

        assertThat(workflow.getStatus()).isEqualTo(WorkflowStatus.RECEIVED);
        assertThat(workflow.getPreviousStatus()).isEqualTo(WorkflowStatus.INITIAL);
        assertThat(workflow.getMessage()).isEqualTo("Intake complete");
    }

    @Test
    void shouldBuildHistoryRecord() {
        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setRequestId("req-1");

        WorkflowHistoryEntity history =
                engine.buildHistoryRecord(workflow, WorkflowStatus.INITIAL, WorkflowStatus.RECEIVED, "ok", "tester");

        assertThat(history.getFromStatus()).isEqualTo(WorkflowStatus.INITIAL);
        assertThat(history.getToStatus()).isEqualTo(WorkflowStatus.RECEIVED);
        assertThat(history.getTransitionReason()).isEqualTo("ok");
        assertThat(history.getTransitionedBy()).isEqualTo("tester");
        assertThat(history.getRequestId()).isEqualTo("req-1");
    }

    @Test
    void shouldIdentifyTerminalStatuses() {
        assertThat(engine.isTerminalStatus(WorkflowStatus.COMPLETED)).isTrue();
        assertThat(engine.isTerminalStatus(WorkflowStatus.CANCELLED)).isTrue();
        assertThat(engine.isTerminalStatus(WorkflowStatus.RECEIVED)).isFalse();
    }
}
