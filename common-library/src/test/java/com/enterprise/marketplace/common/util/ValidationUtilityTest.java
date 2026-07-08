package com.enterprise.marketplace.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enterprise.marketplace.common.exception.MarketplaceException;

import jakarta.validation.Validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ValidationUtilityTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private ValidationUtility validationUtility;

    @Test
    void shouldValidateIndianMobile() {
        assertThatCode(() -> validationUtility.requireIndianMobile("9876543210"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInvalidIndianMobile() {
        assertThatThrownBy(() -> validationUtility.requireIndianMobile("12345"))
                .isInstanceOf(MarketplaceException.class);
    }

    @Test
    void shouldValidateGstin() {
        assertThatCode(() -> validationUtility.requireGstin("27AABCU9603R1ZM"))
                .doesNotThrowAnyException();
    }
}
