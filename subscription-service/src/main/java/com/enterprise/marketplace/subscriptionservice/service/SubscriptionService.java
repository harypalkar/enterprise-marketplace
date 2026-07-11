package com.enterprise.marketplace.subscriptionservice.service;

import com.enterprise.marketplace.subscriptionservice.dto.StatusUpdateRequest;
import com.enterprise.marketplace.subscriptionservice.dto.SubscribeRequest;
import com.enterprise.marketplace.subscriptionservice.dto.SubscriptionPageResponse;
import com.enterprise.marketplace.subscriptionservice.dto.SubscriptionResponse;
import java.util.UUID;

public interface SubscriptionService {

    SubscriptionResponse subscribe(SubscribeRequest request);

    SubscriptionResponse getSubscription(UUID subscriptionId);

    SubscriptionPageResponse getBySeller(UUID sellerId, int page, int size);

    SubscriptionPageResponse getByBuyer(UUID buyerId, int page, int size);

    SubscriptionResponse updateStatus(UUID subscriptionId, StatusUpdateRequest request);

    SubscriptionResponse cancel(UUID subscriptionId);

    SubscriptionResponse renew(UUID subscriptionId);

    void processWorkflowCompleted(String payload);
}
