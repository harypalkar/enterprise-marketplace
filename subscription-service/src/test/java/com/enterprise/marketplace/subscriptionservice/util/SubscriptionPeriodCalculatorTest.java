package com.enterprise.marketplace.subscriptionservice.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.subscriptionservice.enums.BillingCycle;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SubscriptionPeriodCalculatorTest {

    @Test
    void shouldCalculateMonthlyEndDate() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        LocalDate end = SubscriptionPeriodCalculator.calculateEndDate(start, BillingCycle.MONTHLY);
        assertThat(end).isEqualTo(LocalDate.of(2026, 2, 15));
    }

    @Test
    void shouldCalculateYearlyEndDate() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        LocalDate end = SubscriptionPeriodCalculator.calculateEndDate(start, BillingCycle.YEARLY);
        assertThat(end).isEqualTo(LocalDate.of(2027, 1, 15));
    }

    @Test
    void shouldReturnNullForNoneBillingCycle() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        assertThat(SubscriptionPeriodCalculator.calculateEndDate(start, BillingCycle.NONE)).isNull();
    }

    @Test
    void shouldUseTodayWhenEndDatePassed() {
        LocalDate pastEnd = LocalDate.now().minusDays(5);
        assertThat(SubscriptionPeriodCalculator.resolveRenewStartDate(pastEnd)).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldUseFutureEndDateForRenewStart() {
        LocalDate futureEnd = LocalDate.now().plusDays(10);
        assertThat(SubscriptionPeriodCalculator.resolveRenewStartDate(futureEnd)).isEqualTo(futureEnd);
    }
}
