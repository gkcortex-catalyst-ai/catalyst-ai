package com.gkcorex.catalyst.ai.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Plan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  String name;

  @Column(unique = true)
  String stripePriceId;

  Integer maxProjects;

  Integer maxTokensPerDay;

  Integer maxPreviews;

  /*
     Unlimited access to LLM, Ignore maxTokensPerDay if true
  */
  Boolean unlimitedAi;

  Boolean active;
}
