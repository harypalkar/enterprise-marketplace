package com.enterprise.marketplace.sellerservice.domain.service;

import com.enterprise.marketplace.sellerservice.domain.model.Seller;
import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
import org.springframework.util.StringUtils;

/**
 * Pure domain rules for seller validation and status transitions.
 */
public final class SellerDomainService {

    private SellerDomainService() {
    }

    public static void validateForCreate(Seller seller) {
        validateCompanyName(seller.getCompanyName());
        validateTradeName(seller.getTradeName());
        validateGstin(seller.getGstin());
        validatePan(seller.getPan());
        validateEmail(seller.getEmail());
        validatePhone(seller.getPhone());
        validateCity(seller.getCity());
        validateState(seller.getState());
        validateCountry(seller.getCountry());
        validatePinCode(seller.getPinCode());
    }

    public static void validateForUpdate(Seller seller) {
        validateCompanyName(seller.getCompanyName());
        validateTradeName(seller.getTradeName());
        validateGstin(seller.getGstin());
        validatePan(seller.getPan());
        validateEmail(seller.getEmail());
        validatePhone(seller.getPhone());
        validateCity(seller.getCity());
        validateState(seller.getState());
        validateCountry(seller.getCountry());
        validatePinCode(seller.getPinCode());
    }

    public static void validateStatusTransition(SellerStatus current, SellerStatus target) {
        if (current == SellerStatus.ARCHIVED) {
            throw new IllegalArgumentException("Archived sellers cannot change status");
        }
        if (current == target) {
            return;
        }
        if (current == SellerStatus.PENDING && (target == SellerStatus.ACTIVE || target == SellerStatus.ARCHIVED)) {
            return;
        }
        if (current == SellerStatus.ACTIVE
                && (target == SellerStatus.SUSPENDED || target == SellerStatus.ARCHIVED)) {
            return;
        }
        if (current == SellerStatus.SUSPENDED
                && (target == SellerStatus.ACTIVE || target == SellerStatus.ARCHIVED)) {
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

    private static void validateTradeName(String tradeName) {
        if (!StringUtils.hasText(tradeName) || tradeName.length() > 255) {
            throw new IllegalArgumentException("Trade name is required and must be at most 255 characters");
        }
    }

    private static void validateGstin(String gstin) {
        if (!StringUtils.hasText(gstin) || gstin.length() > 15) {
            throw new IllegalArgumentException("GSTIN is required and must be at most 15 characters");
        }
    }

    private static void validatePan(String pan) {
        if (!StringUtils.hasText(pan) || pan.length() > 10) {
            throw new IllegalArgumentException("PAN is required and must be at most 10 characters");
        }
    }

    private static void validateEmail(String email) {
        if (!StringUtils.hasText(email) || email.length() > 255) {
            throw new IllegalArgumentException("Email is required and must be at most 255 characters");
        }
    }

    private static void validatePhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() > 10) {
            throw new IllegalArgumentException("Phone is required and must be at most 10 characters");
        }
    }

    private static void validateCity(String city) {
        if (!StringUtils.hasText(city) || city.length() > 128) {
            throw new IllegalArgumentException("City is required and must be at most 128 characters");
        }
    }

    private static void validateState(String state) {
        if (!StringUtils.hasText(state) || state.length() > 128) {
            throw new IllegalArgumentException("State is required and must be at most 128 characters");
        }
    }

    private static void validateCountry(String country) {
        if (!StringUtils.hasText(country) || country.length() > 128) {
            throw new IllegalArgumentException("Country is required and must be at most 128 characters");
        }
    }

    private static void validatePinCode(String pinCode) {
        if (!StringUtils.hasText(pinCode) || pinCode.length() > 10) {
            throw new IllegalArgumentException("PIN code is required and must be at most 10 characters");
        }
    }
}
