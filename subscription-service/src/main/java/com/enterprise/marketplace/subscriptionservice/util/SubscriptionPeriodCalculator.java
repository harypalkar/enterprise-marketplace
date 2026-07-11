package com.enterprise.marketplace.subscriptionservice.util;

import com.enterprise.marketplace.subscriptionservice.enums.BillingCycle;
import java.time.LocalDate;
import org.springframework.util.StringUtils;

public final class SubscriptionPeriodCalculator {

    private SubscriptionPeriodCalculator() {}

    public static LocalDate calculateEndDate(LocalDate startDate, BillingCycle billingCycle) {
        if (startDate == null || billingCycle == null) {
            return null;
        }
        return switch (billingCycle) {
            case MONTHLY -> startDate.plusMonths(1);
            case YEARLY -> startDate.plusYears(1);
            case NONE -> null;
        };
    }

    public static LocalDate resolveRenewStartDate(LocalDate currentEndDate) {
        LocalDate today = LocalDate.now();
        if (currentEndDate != null && currentEndDate.isAfter(today)) {
            return currentEndDate;
        }
        return today;
    }
}
