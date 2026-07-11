package com.enterprise.marketplace.notificationservice.exception;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;

public class NotificationTemplateException extends MarketplaceException {

    public NotificationTemplateException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }
}
