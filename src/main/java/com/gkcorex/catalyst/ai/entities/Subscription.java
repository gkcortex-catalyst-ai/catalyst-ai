package com.gkcorex.catalyst.ai.entities;

import com.gkcorex.catalyst.ai.enums.SubscriptionStatus;
import jakarta.persistence.*;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "user_id")
  User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "plan_id")
  Plan plan;

  String stripeSubscriptionId;

  @Enumerated(value = EnumType.STRING)
  SubscriptionStatus status;

  Instant currentPeriodStart;

  Instant currentPeriodEnd;

  Boolean cancelAtPeriodEnd = false;

  @CreationTimestamp
  Instant createdAt;

  @UpdateTimestamp
  Instant updatedAt;
}
