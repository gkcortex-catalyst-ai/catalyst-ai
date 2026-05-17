package com.gkcorex.catalyst.ai.dtos.subscription;

import java.time.Instant;

public record SubscriptionResponse(
    PlanResponse plan, String status, Instant currentPeriodEnd, Long tokensUsedThisCycle) {}
