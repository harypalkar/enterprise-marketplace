package com.enterprise.marketplace.notificationservice.channel;

public record ChannelDeliveryResult(boolean success, String response, String errorMessage) {

    public static ChannelDeliveryResult success(String response) {
        return new ChannelDeliveryResult(true, response, null);
    }

    public static ChannelDeliveryResult failure(String errorMessage) {
        return new ChannelDeliveryResult(false, null, errorMessage);
    }
}
