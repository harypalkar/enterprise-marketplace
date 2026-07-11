package com.enterprise.marketplace.notificationservice.provider;

public record ProviderDeliveryResult(boolean success, String response, String errorMessage) {

    public static ProviderDeliveryResult success(String response) {
        return new ProviderDeliveryResult(true, response, null);
    }

    public static ProviderDeliveryResult failure(String errorMessage) {
        return new ProviderDeliveryResult(false, null, errorMessage);
    }
}
