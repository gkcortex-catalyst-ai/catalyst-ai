package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.dtos.subscription.CheckoutRequest;
import com.gkcorex.catalyst.ai.dtos.subscription.CheckoutResponse;
import com.gkcorex.catalyst.ai.dtos.subscription.PortalResponse;
import com.gkcorex.catalyst.ai.entities.Plan;
import com.gkcorex.catalyst.ai.entities.User;
import com.gkcorex.catalyst.ai.enums.SubscriptionStatus;
import com.gkcorex.catalyst.ai.exceptions.BadRequestException;
import com.gkcorex.catalyst.ai.exceptions.ResourceNotFoundException;
import com.gkcorex.catalyst.ai.repositories.PlanRepository;
import com.gkcorex.catalyst.ai.repositories.UserRepository;
import com.gkcorex.catalyst.ai.security.JwtAuthUtil;
import com.gkcorex.catalyst.ai.services.PaymentProcessorService;
import com.gkcorex.catalyst.ai.services.SubscriptionService;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StripePaymentProcessorImpl implements PaymentProcessorService {

  @Value("${client.url}")
  String frontendUrl;

  final JwtAuthUtil jwtAuthUtil;

  final PlanRepository planRepository;

  final UserRepository userRepository;

  final SubscriptionService subscriptionService;

  @Override
  public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest checkoutRequest) {
    Plan plan =
        planRepository
            .findById(checkoutRequest.planId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "plan not found", checkoutRequest.planId().toString()));
    Long userId = jwtAuthUtil.getCurrentUserId();
    User user = getUser(userId);

    SessionCreateParams.Builder builder =
        SessionCreateParams.builder()
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(plan.getStripePriceId())
                    .setQuantity(1L)
                    .build())
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSubscriptionData(
                SessionCreateParams.SubscriptionData.builder()
                    .setBillingMode(
                        SessionCreateParams.SubscriptionData.BillingMode.builder()
                            .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                            .build())
                    .build())
            .setSuccessUrl(frontendUrl + "/success.html?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(frontendUrl + "/cancel.html")
            .putMetadata("user_id", userId.toString())
            .putMetadata("plan_id", plan.getId().toString());

    try {
      String stripeCustomerId = user.getStripeCustomerId();
      if (Objects.isNull(stripeCustomerId) || stripeCustomerId.isEmpty())
        builder.setCustomerEmail(user.getUsername());
      else builder.setCustomer(stripeCustomerId);
      SessionCreateParams params = builder.build();
      Session session = Session.create(params);
      return new CheckoutResponse(session.getUrl());
    } catch (StripeException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public PortalResponse openCustomerPortal() {
    Long userId = jwtAuthUtil.getCurrentUserId();
    User user = getUser(userId);

    String stripeCustomerId = user.getStripeCustomerId();

    if (stripeCustomerId == null || stripeCustomerId.isEmpty())
      throw new BadRequestException("User does not have stripe customer id: " + userId);

    try {
      var portalSession =
          com.stripe.model.billingportal.Session.create(
              com.stripe.param.billingportal.SessionCreateParams.builder()
                  .setCustomer(stripeCustomerId)
                  .setReturnUrl(frontendUrl)
                  .build());
      return new PortalResponse(portalSession.getUrl());
    } catch (StripeException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void handleWebhookEvent(
      String type, StripeObject stripeObject, Map<String, String> metaData) {
    log.debug("Handling Stripe Events: {}", type);
    switch (type) {
        // one time, on checkout completed
      case "checkout.session.completed" ->
          handleCheckoutSessionCompleted((Session) stripeObject, metaData);
        // when user cancels, upgrades or any updates
      case "customer.subscription.updated" ->
          handleCustomerSubscriptionUpdated((Subscription) stripeObject);
        // when subscription ends, revoke the access
      case "customer.subscription.deleted" ->
          handleCustomerSubscriptionDeleted((Subscription) stripeObject);
        // when invoice is paid
      case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject);
        // when invoice is not paid, mark as PAST_DUE
      case "invoice.payment_failed" -> handelInvoicePaymentFailed((Invoice) stripeObject);
      default -> log.debug("Ignoring event: {}", type);
    }
  }

  private void handleCheckoutSessionCompleted(Session session, Map<String, String> metadata) {
    if (Objects.isNull(session)) {
      log.error("session object is null inside handleCheckoutSessionCompleted");
      return;
    }

    Long userId = Long.parseLong(metadata.get("user_id"));
    Long planId = Long.parseLong(metadata.get("plan_id"));

    String subscriptionId = session.getSubscription();
    String customerId = session.getCustomer();
    User user = getUser(userId);

    if (Objects.isNull(user.getStripeCustomerId())) {
      user.setStripeCustomerId(customerId);
      userRepository.save(user);
    }
    subscriptionService.activateSubscription(userId, planId, subscriptionId, customerId);
  }

  private void handleCustomerSubscriptionUpdated(Subscription subscription) {
    if (Objects.isNull(subscription)) {
      log.error("subscription object is null inside handleCustomerSubscriptionUpdated");
      return;
    }

    SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());
    if (Objects.isNull(status)) {
      log.warn(
          "Unknown status '{}' for subscription '{}'",
          subscription.getStatus(),
          subscription.getId());
      return;
    }

    SubscriptionItem item = subscription.getItems().getData().get(0);
    Instant periodStart = toInstant(item.getCurrentPeriodStart());
    Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

    Long planId = resolvePlanId(item.getPrice());

    subscriptionService.updateSubscription(
        subscription.getId(),
        status,
        periodStart,
        periodEnd,
        planId,
        subscription.getCancelAtPeriodEnd());
  }

  private void handleCustomerSubscriptionDeleted(Subscription subscription) {
    if (Objects.isNull(subscription)) {
      log.error("subscription object is null inside handleCustomerSubscriptionDeleted");
      return;
    }

    subscriptionService.cancelSubscription(subscription.getId());
  }

  private void handleInvoicePaid(Invoice invoice) {
    String subscriptionId = extractSubscriptionId(invoice);
    if (subscriptionId == null) return;
    try {
      Subscription subscription = Subscription.retrieve(subscriptionId);

      var item = subscription.getItems().getData().get(0);
      Instant periodStart = toInstant(item.getCurrentPeriodStart());
      Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

      subscriptionService.renewSubscriptionPeriod(subscriptionId, periodStart, periodEnd);
    } catch (StripeException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void handelInvoicePaymentFailed(Invoice invoice) {
    String subscriptionId = extractSubscriptionId(invoice);
    if (subscriptionId == null) return;
    subscriptionService.markSubscriptionPastDue(subscriptionId);
  }

  private User getUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found", userId.toString()));
  }

  private SubscriptionStatus mapStripeStatusToEnum(String status) {
    return switch (status) {
      case "active" -> SubscriptionStatus.ACTIVE;
      case "trailing" -> SubscriptionStatus.TRAILING;
      case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
      case "canceled" -> SubscriptionStatus.CANCELLED;
      case "incomplete" -> SubscriptionStatus.INCOMPLETE;
      default -> {
        log.warn("Unmapped Stripe status: {}", status);
        yield null;
      }
    };
  }

  private Instant toInstant(Long epoch) {
    return epoch != null ? Instant.ofEpochSecond(epoch) : null;
  }

  private Long resolvePlanId(Price price) {
    if (Objects.isNull(price) || Objects.isNull(price.getId())) return null;
    return planRepository.findByStripePriceId(price.getId()).map(Plan::getId).orElse(null);
  }

  private String extractSubscriptionId(Invoice invoice) {
    var parent = invoice.getParent();
    if (parent == null) return null;

    var subDetails = parent.getSubscriptionDetails();
    if (subDetails == null) return null;

    return subDetails.getSubscription();
  }
}
