package com.enterprise.marketplace.productservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonSchemaValidationStep implements ValidationStep {

    private static final String SCHEMA_PATH = "schemas/product-canonical-schema.json";

    private final ObjectMapper objectMapper;
    private volatile JsonSchema jsonSchema;

    @Override
    public void validate(ProductValidationContext context) {
        if (context.operation() == ValidationOperation.DELETE
                || context.operation() == ValidationOperation.PATCH) {
            return;
        }
        try {
            JsonNode requestNode = objectMapper.valueToTree(context.request());
            Set<ValidationMessage> errors = schema().validate(requestNode);
            if (!errors.isEmpty()) {
                String message = errors.stream()
                        .map(ValidationMessage::getMessage)
                        .collect(Collectors.joining("; "));
                throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "JSON schema validation failed: " + message);
            }
        } catch (MarketplaceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MarketplaceException(
                    ErrorCode.VALIDATION_ERROR, "Unable to validate request against JSON schema", ex);
        }
    }

    private JsonSchema schema() throws Exception {
        if (jsonSchema == null) {
            synchronized (this) {
                if (jsonSchema == null) {
                    try (InputStream inputStream = new ClassPathResource(SCHEMA_PATH).getInputStream()) {
                        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
                        jsonSchema = factory.getSchema(inputStream);
                    }
                }
            }
        }
        return jsonSchema;
    }
}
