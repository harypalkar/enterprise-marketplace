package com.enterprise.marketplace.reportservice.constants;

import java.util.UUID;

public final class ReportCacheKeys {

    public static final String JOB_PREFIX = "report:job:";
    public static final String RESULT_PREFIX = "report:result:";
    public static final String DEFINITION_PREFIX = "report:definition:";
    public static final String DEFINITIONS_ALL = "report:definitions:all";

    private ReportCacheKeys() {}

    public static String jobKey(UUID jobId) {
        return JOB_PREFIX + jobId;
    }

    public static String resultKey(UUID jobId) {
        return RESULT_PREFIX + jobId;
    }

    public static String definitionKey(String reportCode) {
        return DEFINITION_PREFIX + reportCode;
    }
}
