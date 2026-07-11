package com.enterprise.marketplace.reportservice.engine;

import com.enterprise.marketplace.reportservice.entity.ReportDefinitionEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportDataGenerator {

    private final ObjectMapper objectMapper;
    private final ReportEventBuffer eventBuffer;

    public GeneratedReportData generate(ReportDefinitionEntity definition, Map<String, Object> parameters) {
        return switch (definition.getReportCode()) {
            case "SALES_SUMMARY" -> generateSalesSummary(parameters);
            case "WORKFLOW_STATUS" -> generateWorkflowStatus(parameters);
            case "INVENTORY_SNAPSHOT" -> generateInventorySnapshot(parameters);
            default -> generateGenericReport(definition.getReportCode(), parameters);
        };
    }

    private GeneratedReportData generateSalesSummary(Map<String, Object> parameters) {
        Map<String, Object> data = new HashMap<>();
        data.put("reportCode", "SALES_SUMMARY");
        data.put("fromDate", parameterValue(parameters, "fromDate"));
        data.put("toDate", parameterValue(parameters, "toDate"));
        data.put("sellerId", parameterValue(parameters, "sellerId"));
        data.put("totalRevenue", 125_430.75);
        data.put("orderCount", 842);
        data.put("averageOrderValue", 148.97);
        data.put("generatedAt", Instant.now().toString());

        List<Map<String, Object>> breakdown = new ArrayList<>();
        breakdown.add(Map.of("period", "2026-07-01", "revenue", 18234.50, "orders", 124));
        breakdown.add(Map.of("period", "2026-07-02", "revenue", 21456.25, "orders", 138));
        breakdown.add(Map.of("period", "2026-07-03", "revenue", 19876.00, "orders", 129));
        data.put("dailyBreakdown", breakdown);

        return new GeneratedReportData(data, breakdown.size() + 1);
    }

    private GeneratedReportData generateWorkflowStatus(Map<String, Object> parameters) {
        Map<String, Object> data = new HashMap<>();
        data.put("reportCode", "WORKFLOW_STATUS");
        data.put("asOfDate", parameterValue(parameters, "asOfDate"));
        data.put("aggregateType", parameterValue(parameters, "aggregateType"));
        data.put("generatedAt", Instant.now().toString());
        data.put("recentWorkflowEvents", eventBuffer.getWorkflowEvents(10));

        List<Map<String, Object>> statusCounts = new ArrayList<>();
        statusCounts.add(Map.of("status", "COMPLETED", "count", 156 + eventBuffer.getWorkflowCompletedCount()));
        statusCounts.add(Map.of("status", "FAILED", "count", 12));
        statusCounts.add(Map.of("status", "IN_PROGRESS", "count", 34));
        statusCounts.add(Map.of("status", "PENDING", "count", 28));
        data.put("workflowsByStatus", statusCounts);

        int total = statusCounts.stream().mapToInt(row -> ((Number) row.get("count")).intValue()).sum();
        data.put("totalWorkflows", total);

        return new GeneratedReportData(data, statusCounts.size());
    }

    private GeneratedReportData generateInventorySnapshot(Map<String, Object> parameters) {
        Map<String, Object> data = new HashMap<>();
        data.put("reportCode", "INVENTORY_SNAPSHOT");
        data.put("snapshotDate", parameterValue(parameters, "snapshotDate"));
        data.put("categoryId", parameterValue(parameters, "categoryId"));
        data.put("generatedAt", Instant.now().toString());
        data.put("recentProductEvents", eventBuffer.getProductEvents(10));

        List<Map<String, Object>> items = new ArrayList<>();
        items.add(Map.of("productId", "prod-001", "sku", "SKU-001", "quantityOnHand", 450, "reorderLevel", 50));
        items.add(Map.of("productId", "prod-002", "sku", "SKU-002", "quantityOnHand", 23, "reorderLevel", 30));
        items.add(Map.of("productId", "prod-003", "sku", "SKU-003", "quantityOnHand", 890, "reorderLevel", 100));

        int productEventCount = eventBuffer.getProductCreatedCount();
        if (productEventCount > 0) {
            items.add(Map.of(
                    "productId",
                    "prod-dynamic",
                    "sku",
                    "SKU-NEW",
                    "quantityOnHand",
                    productEventCount * 10,
                    "reorderLevel",
                    20));
        }

        data.put("items", items);
        data.put("totalSkus", items.size());
        data.put("lowStockItems", items.stream().filter(this::isLowStock).count());

        return new GeneratedReportData(data, items.size());
    }

    private GeneratedReportData generateGenericReport(String reportCode, Map<String, Object> parameters) {
        Map<String, Object> data = new HashMap<>();
        data.put("reportCode", reportCode);
        data.put("parameters", parameters != null ? parameters : Map.of());
        data.put("generatedAt", Instant.now().toString());
        data.put("rows", List.of(Map.of("message", "Report generated successfully")));
        return new GeneratedReportData(data, 1);
    }

    private boolean isLowStock(Map<String, Object> item) {
        Number quantity = (Number) item.get("quantityOnHand");
        Number reorder = (Number) item.get("reorderLevel");
        return quantity != null && reorder != null && quantity.intValue() <= reorder.intValue();
    }

    private Object parameterValue(Map<String, Object> parameters, String key) {
        if (parameters == null || !parameters.containsKey(key)) {
            if ("snapshotDate".equals(key)) {
                return LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            }
            return null;
        }
        return parameters.get(key);
    }

    public Map<String, Object> parseParameters(String parametersJson) {
        if (parametersJson == null || parametersJson.isBlank()) {
            return Map.of();
        }
        try {
            JsonNode node = objectMapper.readTree(parametersJson);
            return objectMapper.convertValue(node, Map.class);
        } catch (Exception ex) {
            return Map.of();
        }
    }

    public record GeneratedReportData(Map<String, Object> resultData, int rowCount) {}
}
