package com.enterprise.marketplace.reportservice.constants;

public final class ReportKafkaTopics {

    public static final String REPORT_GENERATED = "report-generated";
    public static final String REPORT_FAILED = "report-failed";
    public static final String AUDIT_CREATED = "audit-created";
    public static final String WORKFLOW_COMPLETED = "workflow-completed";
    public static final String PRODUCT_CREATED = "product-created";
    public static final String DEAD_LETTER = "report-dead-letter";

    private ReportKafkaTopics() {}
}
