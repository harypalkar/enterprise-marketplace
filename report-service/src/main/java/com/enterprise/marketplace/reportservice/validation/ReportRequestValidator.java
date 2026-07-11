package com.enterprise.marketplace.reportservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.reportservice.dto.CreateReportJobRequest;
import com.enterprise.marketplace.reportservice.entity.ReportDefinitionEntity;
import com.enterprise.marketplace.reportservice.repository.ReportDefinitionRepository;
import com.enterprise.marketplace.reportservice.repository.ReportJobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ReportRequestValidator {

    private final ReportDefinitionRepository definitionRepository;
    private final ReportJobRepository jobRepository;
    private final ObjectMapper objectMapper;

    public ReportDefinitionEntity validateCreateJobRequest(CreateReportJobRequest request) {
        if (!StringUtils.hasText(request.getRequestId())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "requestId is required");
        }
        if (!StringUtils.hasText(request.getReportCode())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "reportCode is required");
        }

        jobRepository.findByRequestIdAndActiveTrue(request.getRequestId()).ifPresent(existing -> {
            throw new MarketplaceException(
                    ErrorCode.CONFLICT, "Report job already exists for requestId " + request.getRequestId());
        });

        ReportDefinitionEntity definition = definitionRepository
                .findByReportCodeAndActiveTrue(request.getReportCode().trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Report definition not found or inactive: " + request.getReportCode()));

        validateParameters(definition, request.getParameters());
        return definition;
    }

    private void validateParameters(ReportDefinitionEntity definition, Map<String, Object> parameters) {
        if (definition.getParametersSchema() == null || definition.getParametersSchema().isBlank()) {
            return;
        }
        try {
            JsonNode schema = objectMapper.readTree(definition.getParametersSchema());
            JsonNode required = schema.get("required");
            if (required != null && required.isArray()) {
                for (Iterator<JsonNode> it = required.elements(); it.hasNext(); ) {
                    String field = it.next().asText();
                    if (parameters == null || !parameters.containsKey(field) || parameters.get(field) == null) {
                        throw new MarketplaceException(
                                ErrorCode.VALIDATION_ERROR, "Missing required parameter: " + field);
                    }
                }
            }
        } catch (MarketplaceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Invalid parameters schema configuration");
        }
    }
}
