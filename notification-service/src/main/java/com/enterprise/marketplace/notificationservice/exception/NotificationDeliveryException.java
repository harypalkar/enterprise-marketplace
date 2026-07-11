package com.enterprise.marketplace.notificationservice.exception;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;

public class NotificationDeliveryException extends MarketplaceException {

    public NotificationDeliveryException(String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }

    public NotificationDeliveryException(String message, Throwable cause) {
        super(ErrorCode.INTERNAL_ERROR, message, cause);
    }
}
