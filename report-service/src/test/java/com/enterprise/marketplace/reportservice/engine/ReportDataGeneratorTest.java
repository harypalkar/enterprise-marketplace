package com.enterprise.marketplace.reportservice.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.reportservice.entity.ReportDefinitionEntity;
import com.enterprise.marketplace.reportservice.enums.ReportType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

class ReportDataGeneratorTest {

    private ReportDataGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ReportDataGenerator(new ObjectMapper(), new ReportEventBuffer());
    }

    @Test
    void shouldGenerateSalesSummaryReport() {
        ReportDefinitionEntity definition = new ReportDefinitionEntity();
        definition.setReportCode("SALES_SUMMARY");
        definition.setReportType(ReportType.ANALYTICS);

        ReportDataGenerator.GeneratedReportData data = generator.generate(
                definition, Map.of("fromDate", "2026-07-01", "toDate", "2026-07-09"));

        assertThat(data.resultData()).containsKey("totalRevenue");
        assertThat(data.resultData()).containsKey("dailyBreakdown");
        assertThat(data.rowCount()).isPositive();
    }

    @Test
    void shouldGenerateWorkflowStatusReport() {
        ReportDefinitionEntity definition = new ReportDefinitionEntity();
        definition.setReportCode("WORKFLOW_STATUS");
        definition.setReportType(ReportType.OPERATIONAL);

        ReportDataGenerator.GeneratedReportData data = generator.generate(
                definition, Map.of("asOfDate", "2026-07-09T00:00:00Z"));

        assertThat(data.resultData()).containsKey("workflowsByStatus");
        assertThat(data.resultData()).containsKey("totalWorkflows");
    }

    @Test
    void shouldGenerateInventorySnapshotReport() {
        ReportDefinitionEntity definition = new ReportDefinitionEntity();
        definition.setReportCode("INVENTORY_SNAPSHOT");
        definition.setReportType(ReportType.SNAPSHOT);

        ReportDataGenerator.GeneratedReportData data = generator.generate(
                definition, Map.of("snapshotDate", "2026-07-09"));

        assertThat(data.resultData()).containsKey("items");
        assertThat(data.resultData()).containsKey("totalSkus");
    }
}
