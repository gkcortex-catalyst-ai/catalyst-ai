package com.gkcorex.catalyst.ai.repositories;

import com.gkcorex.catalyst.ai.entities.Subscription;
import com.gkcorex.catalyst.ai.enums.SubscriptionStatus;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
  Optional<Subscription> findByUserIdAndStatusIn(Long userId, Set<SubscriptionStatus> status);

  boolean existsByStripeSubscriptionId(String subscriptionId);

  Optional<Subscription> findByStripeSubscriptionId(String subscriptionId);
}
