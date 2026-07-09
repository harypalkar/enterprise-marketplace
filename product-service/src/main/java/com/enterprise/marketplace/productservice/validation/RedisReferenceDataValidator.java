package com.enterprise.marketplace.productservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.productservice.constants.RedisCacheKeys;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "marketplace.redis.enabled", havingValue = "true")
public class RedisReferenceDataValidator implements ReferenceDataValidator {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void validateSeller(UUID sellerId) {
        if (sellerId == null) {
            return;
        }
        Boolean exists = redisTemplate.hasKey(RedisCacheKeys.SELLER_PREFIX + sellerId);
        if (!Boolean.TRUE.equals(exists)) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Unknown seller: " + sellerId);
        }
    }

    @Override
    public void validateCategory(UUID categoryId) {
        if (categoryId == null) {
            return;
        }
        Boolean exists = redisTemplate.hasKey(RedisCacheKeys.CATEGORY_PREFIX + categoryId);
        if (!Boolean.TRUE.equals(exists)) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Unknown category: " + categoryId);
        }
    }

    @Override
    public void validateCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            return;
        }
        Boolean exists = redisTemplate.hasKey(RedisCacheKeys.CURRENCY_PREFIX + currency.toUpperCase());
        if (!Boolean.TRUE.equals(exists)) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Unknown currency: " + currency);
        }
    }
}
