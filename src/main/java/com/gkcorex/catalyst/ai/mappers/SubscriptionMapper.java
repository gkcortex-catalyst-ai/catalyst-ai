package com.gkcorex.catalyst.ai.mappers;

import com.gkcorex.catalyst.ai.dtos.subscription.PlanResponse;
import com.gkcorex.catalyst.ai.dtos.subscription.SubscriptionResponse;
import com.gkcorex.catalyst.ai.entities.Plan;
import com.gkcorex.catalyst.ai.entities.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

  SubscriptionResponse mapSubscriptionToSubscriptionResponse(Subscription subscription);

  PlanResponse mapPlanToPlanResponse(Plan plan);
}
