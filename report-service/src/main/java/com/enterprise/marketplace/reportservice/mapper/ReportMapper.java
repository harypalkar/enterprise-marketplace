package com.enterprise.marketplace.reportservice.mapper;

import com.enterprise.marketplace.reportservice.dto.ReportDefinitionResponse;
import com.enterprise.marketplace.reportservice.dto.ReportJobResponse;
import com.enterprise.marketplace.reportservice.dto.ReportResultResponse;
import com.enterprise.marketplace.reportservice.entity.ReportDefinitionEntity;
import com.enterprise.marketplace.reportservice.entity.ReportJobEntity;
import com.enterprise.marketplace.reportservice.entity.ReportResultEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportMapper {

    private final ObjectMapper objectMapper;

    public ReportJobResponse toJobResponse(ReportJobEntity entity) {
        return ReportJobResponse.builder()
                .id(entity.getId())
                .requestId(entity.getRequestId())
                .reportCode(entity.getReportCode())
                .requestedBy(entity.getRequestedBy())
                .status(entity.getStatus())
                .parameters(deserializeJsonMap(entity.getParameters()))
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ReportDefinitionResponse toDefinitionResponse(ReportDefinitionEntity entity) {
        return ReportDefinitionResponse.builder()
                .id(entity.getId())
                .reportCode(entity.getReportCode())
                .name(entity.getName())
                .reportType(entity.getReportType())
                .queryTemplate(entity.getQueryTemplate())
                .parametersSchema(deserializeJsonMap(entity.getParametersSchema()))
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ReportResultResponse toResultResponse(ReportResultEntity entity) {
        return ReportResultResponse.builder()
                .id(entity.getId())
                .jobId(entity.getJobId())
                .resultData(deserializeJsonMap(entity.getResultData()))
                .rowCount(entity.getRowCount())
                .fileUrl(entity.getFileUrl())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private Map<String, Object> deserializeJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}
