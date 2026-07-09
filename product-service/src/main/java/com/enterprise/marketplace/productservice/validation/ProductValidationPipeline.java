package com.enterprise.marketplace.productservice.validation;

import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class ProductValidationPipeline {

    private final List<ValidationStep> steps;

    public ProductValidationPipeline(
            JwtValidationStep jwtValidationStep,
            JsonSchemaValidationStep jsonSchemaValidationStep,
            BeanValidationStep beanValidationStep,
            ObjectProvider<ReferenceValidationStep> referenceValidationStepProvider,
            BusinessValidationStep businessValidationStep,
            DuplicateSkuValidationStep duplicateSkuValidationStep) {
        ValidationStep referenceStep =
                referenceValidationStepProvider.getIfAvailable() != null
                        ? referenceValidationStepProvider.getIfAvailable()
                        : context -> {};
        this.steps = List.of(
                jwtValidationStep,
                jsonSchemaValidationStep,
                beanValidationStep,
                referenceStep,
                businessValidationStep,
                duplicateSkuValidationStep);
    }

    public void validate(ProductValidationContext context) {
        for (ValidationStep step : steps) {
            step.validate(context);
        }
    }
}
