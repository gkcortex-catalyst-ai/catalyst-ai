package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.dtos.subscription.SubscriptionResponse;
import com.gkcorex.catalyst.ai.entities.Plan;
import com.gkcorex.catalyst.ai.entities.Subscription;
import com.gkcorex.catalyst.ai.entities.User;
import com.gkcorex.catalyst.ai.enums.SubscriptionStatus;
import com.gkcorex.catalyst.ai.exceptions.ResourceNotFoundException;
import com.gkcorex.catalyst.ai.mappers.SubscriptionMapper;
import com.gkcorex.catalyst.ai.repositories.PlanRepository;
import com.gkcorex.catalyst.ai.repositories.ProjectMemberRepository;
import com.gkcorex.catalyst.ai.repositories.SubscriptionRepository;
import com.gkcorex.catalyst.ai.repositories.UserRepository;
import com.gkcorex.catalyst.ai.security.JwtAuthUtil;
import com.gkcorex.catalyst.ai.services.SubscriptionService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SubscriptionServiceImpl implements SubscriptionService {

  // todo: update allowed projects in prod
  private final Integer FREE_TIER_PROJECTS_ALLOWED = 100;

  JwtAuthUtil jwtAuthUtil;

  SubscriptionRepository subscriptionRepository;

  SubscriptionMapper subscriptionMapper;

  UserRepository userRepository;

  PlanRepository planRepository;

  ProjectMemberRepository projectMemberRepository;

  @Override
  public SubscriptionResponse getCurrentSubscription() {
    Long userId = jwtAuthUtil.getCurrentUserId();
    var currentSubscription =
        subscriptionRepository
            .findByUserIdAndStatusIn(
                userId,
                Set.of(
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PAST_DUE,
                    SubscriptionStatus.TRAILING))
            .orElse(new Subscription());
    return subscriptionMapper.mapSubscriptionToSubscriptionResponse(currentSubscription);
  }

  @Override
  public void activateSubscription(
      Long userId, Long planId, String subscriptionId, String customerId) {
    boolean exists = subscriptionRepository.existsByStripeSubscriptionId(subscriptionId);
    if (exists) return;
    User user = getUser(userId);
    Plan plan = getPlan(planId);

    Subscription subscription =
        Subscription.builder()
            .user(user)
            .plan(plan)
            .stripeSubscriptionId(subscriptionId)
            .status(SubscriptionStatus.INCOMPLETE)
            .build();
    subscriptionRepository.save(subscription);
  }

  @Override
  @Transactional
  public void updateSubscription(
      String subscriptionId,
      SubscriptionStatus status,
      Instant periodStart,
      Instant periodEnd,
      Long planId,
      Boolean cancelAtPeriodEnd) {
    Subscription subscription = getSubscription(subscriptionId);

    boolean isSubscriptionUpdated = false;
    if (status != null && status != subscription.getStatus()) {
      subscription.setStatus(status);
      isSubscriptionUpdated = true;
    }

    if (periodStart != null && periodStart != subscription.getCurrentPeriodStart()) {
      subscription.setCurrentPeriodStart(periodStart);
      isSubscriptionUpdated = true;
    }

    if (periodEnd != null && periodEnd != subscription.getCurrentPeriodEnd()) {
      subscription.setCurrentPeriodEnd(periodEnd);
      isSubscriptionUpdated = true;
    }

    if (cancelAtPeriodEnd != null && cancelAtPeriodEnd != subscription.getCancelAtPeriodEnd()) {
      subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
      isSubscriptionUpdated = true;
    }

    if (planId != null && !planId.equals(subscription.getPlan().getId())) {
      Plan newPlan = getPlan(planId);
      subscription.setPlan(newPlan);
      isSubscriptionUpdated = true;
    }

    if (isSubscriptionUpdated) {
      log.debug("subscription has been updated: {}", subscriptionId);
      subscriptionRepository.save(subscription);
    }
  }

  @Override
  public void cancelSubscription(String subscriptionId) {
    Subscription subscription = getSubscription(subscriptionId);
    subscription.setStatus(SubscriptionStatus.CANCELLED);
    subscriptionRepository.save(subscription);
  }

  @Override
  public void renewSubscriptionPeriod(
      String subscriptionId, Instant periodStart, Instant periodEnd) {
    Subscription subscription = getSubscription(subscriptionId);
    Instant newStart = periodStart != null ? periodStart : subscription.getCurrentPeriodEnd();
    subscription.setCurrentPeriodStart(newStart);
    subscription.setCurrentPeriodEnd(periodEnd);
    if (subscription.getStatus() == SubscriptionStatus.PAST_DUE
        || subscription.getStatus() == SubscriptionStatus.ACTIVE)
      subscription.setStatus(SubscriptionStatus.ACTIVE);
    subscriptionRepository.save(subscription);
  }

  @Override
  public void markSubscriptionPastDue(String subscriptionId) {
    Subscription subscription = getSubscription(subscriptionId);
    if (subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
      log.info("subscription is already due for subscription id: {}", subscriptionId);
      return;
    }
    subscription.setStatus(SubscriptionStatus.PAST_DUE);
    subscriptionRepository.save(subscription);
  }

  @Override
  public boolean canCreateNewProject() {
    Long userId = jwtAuthUtil.getCurrentUserId();
    SubscriptionResponse currentSubscription = getCurrentSubscription();
    int countOfOwnedProjects = projectMemberRepository.countProjectOwnedByUser(userId);
    if (currentSubscription.plan() == null)
      return countOfOwnedProjects < FREE_TIER_PROJECTS_ALLOWED;
    return countOfOwnedProjects < currentSubscription.plan().maxProjects();
  }

  //    Utility Methods
  private User getUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("user not found", userId.toString()));
  }

  private Subscription getSubscription(String subscriptionId) {
    return subscriptionRepository
        .findByStripeSubscriptionId(subscriptionId)
        .orElseThrow(() -> new ResourceNotFoundException("subscription not found", subscriptionId));
  }

  private Plan getPlan(Long planId) {
    return planRepository
        .findById(planId)
        .orElseThrow(() -> new ResourceNotFoundException("plan not found", planId.toString()));
  }
}
