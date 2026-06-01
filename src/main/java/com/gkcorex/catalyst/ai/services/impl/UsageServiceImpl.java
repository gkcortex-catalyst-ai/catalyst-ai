package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.dtos.subscription.PlanResponse;
import com.gkcorex.catalyst.ai.dtos.subscription.SubscriptionResponse;
import com.gkcorex.catalyst.ai.entities.UsageLog;
import com.gkcorex.catalyst.ai.repositories.UsageLogRepository;
import com.gkcorex.catalyst.ai.security.JwtAuthUtil;
import com.gkcorex.catalyst.ai.services.SubscriptionService;
import com.gkcorex.catalyst.ai.services.UsageService;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UsageServiceImpl implements UsageService {

  UsageLogRepository usageLogRepository;

  JwtAuthUtil jwtAuthUtil;

  SubscriptionService subscriptionService;

  @Override
  public void recordTokensUsage(Long userId, int actualTokens) {
    LocalDate today = LocalDate.now();
    UsageLog todayLog =
        usageLogRepository
            .findByUserIdAndDate(userId, today)
            .orElseGet(() -> createNewDailyUsageLog(userId, today));
    todayLog.setTokensUsed(todayLog.getTokensUsed() + actualTokens);
    usageLogRepository.save(todayLog);
  }

  @Override
  public void checkDailyTokensUsage() {
    Long userId = jwtAuthUtil.getCurrentUserId();
    SubscriptionResponse subscriptionResponse = subscriptionService.getCurrentSubscription();
    PlanResponse plan = subscriptionResponse.plan();

    LocalDate today = LocalDate.now();
    UsageLog todayLog =
        usageLogRepository
            .findByUserIdAndDate(userId, today)
            .orElseGet(() -> createNewDailyUsageLog(userId, today));

    if (plan.unlimitedAi()) return;

    int currentUsage = todayLog.getTokensUsed();
    int limit = plan.maxTokensPerDay();

    if (currentUsage >= limit) {
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "Daily limit reached, Upgrade now");
    }
  }

  private UsageLog createNewDailyUsageLog(Long userId, LocalDate date) {
    UsageLog usageLog = UsageLog.builder().userId(userId).date(date).tokensUsed(0).build();
    return usageLogRepository.save(usageLog);
  }
}
