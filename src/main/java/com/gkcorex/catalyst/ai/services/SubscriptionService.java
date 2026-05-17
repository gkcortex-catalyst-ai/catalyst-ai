package com.gkcorex.catalyst.ai.services;

import com.gkcorex.catalyst.ai.dtos.subscription.SubscriptionResponse;
import com.gkcorex.catalyst.ai.enums.SubscriptionStatus;
import java.time.Instant;

public interface SubscriptionService {

  SubscriptionResponse getCurrentSubscription();

  void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId);

  void updateSubscription(
      String subscriptionId,
      SubscriptionStatus status,
      Instant periodStart,
      Instant periodEnd,
      Long planId,
      Boolean cancelAtPeriodEnd);

    void cancelSubscription(String subscriptionId);

    void renewSubscriptionPeriod(String subscriptionId, Instant periodStart, Instant periodEnd);

    void markSubscriptionPastDue(String subscriptionId);

    boolean canCreateNewProject();
}
