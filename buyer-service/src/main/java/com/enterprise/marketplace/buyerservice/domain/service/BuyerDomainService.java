package com.enterprise.marketplace.buyerservice.domain.service;

import com.enterprise.marketplace.buyerservice.domain.model.Buyer;
import com.enterprise.marketplace.buyerservice.domain.model.BuyerStatus;
import org.springframework.util.StringUtils;

/**
 * Pure domain rules for buyer validation and status transitions.
 */
public final class BuyerDomainService {

    private BuyerDomainService() {
    }

    public static void validateForCreate(Buyer buyer) {
        validateCompanyName(buyer.getCompanyName());
        validateContactPerson(buyer.getContactPerson());
        validateEmail(buyer.getEmail());
        validatePhone(buyer.getPhone());
        validateCity(buyer.getCity());
        validateState(buyer.getState());
        validateCountry(buyer.getCountry());
        validatePinCode(buyer.getPinCode());
    }

    public static void validateForUpdate(Buyer buyer) {
        validateCompanyName(buyer.getCompanyName());
        validateContactPerson(buyer.getContactPerson());
        validateEmail(buyer.getEmail());
        validatePhone(buyer.getPhone());
        validateCity(buyer.getCity());
        validateState(buyer.getState());
        validateCountry(buyer.getCountry());
        validatePinCode(buyer.getPinCode());
    }

    public static void validateStatusTransition(BuyerStatus current, BuyerStatus target) {
        if (current == BuyerStatus.ARCHIVED) {
            throw new IllegalArgumentException("Archived buyers cannot change status");
        }
        if (current == target) {
            return;
        }
        if ((current == BuyerStatus.ACTIVE && target == BuyerStatus.INACTIVE)
                || (current == BuyerStatus.INACTIVE && target == BuyerStatus.ACTIVE)
                || target == BuyerStatus.ARCHIVED) {
            return;
        }
        throw new IllegalArgumentException(
                String.format("Invalid status transition from %s to %s", current, target));
    }

    private static void validateCompanyName(String companyName) {
        if (!StringUtils.hasText(companyName) || companyName.length() > 255) {
            throw new IllegalArgumentException("Company name is required and must be at most 255 characters");
        }
    }

    private static void validateContactPerson(String contactPerson) {
        if (!StringUtils.hasText(contactPerson) || contactPerson.length() > 128) {
            throw new IllegalArgumentException("Contact person is required and must be at most 128 characters");
        }
    }

    private static void validateEmail(String email) {
        if (!StringUtils.hasText(email) || email.length() > 255) {
            throw new IllegalArgumentException("Email is required and must be at most 255 characters");
        }
    }

    private static void validatePhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() > 32) {
            throw new IllegalArgumentException("Phone is required and must be at most 32 characters");
        }
    }

    private static void validateCity(String city) {
        if (!StringUtils.hasText(city) || city.length() > 100) {
            throw new IllegalArgumentException("City is required and must be at most 100 characters");
        }
    }

    private static void validateState(String state) {
        if (!StringUtils.hasText(state) || state.length() > 100) {
            throw new IllegalArgumentException("State is required and must be at most 100 characters");
        }
    }

    private static void validateCountry(String country) {
        if (!StringUtils.hasText(country) || country.length() > 100) {
            throw new IllegalArgumentException("Country is required and must be at most 100 characters");
        }
    }

    private static void validatePinCode(String pinCode) {
        if (!StringUtils.hasText(pinCode) || pinCode.length() > 12) {
            throw new IllegalArgumentException("Pin code is required and must be at most 12 characters");
        }
    }
}
