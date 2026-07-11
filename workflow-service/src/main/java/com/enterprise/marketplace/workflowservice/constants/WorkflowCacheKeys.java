package com.enterprise.marketplace.workflowservice.constants;

public final class WorkflowCacheKeys {

    public static final String WORKFLOW_PREFIX = "workflow:";
    public static final String TRANSITION_RULES = "workflow:transition-rules";
    public static final String STATUS_CONFIG = "workflow:status-config";

    private WorkflowCacheKeys() {}

    public static String workflowKey(java.util.UUID workflowId) {
        return WORKFLOW_PREFIX + workflowId;
    }
}
