package com.gkcorex.catalyst.ai.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(
    name = "usage_logs",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "date"})})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsageLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(name = "user_id", nullable = false)
  Long userId;

  Integer tokensUsed;

  @Column(nullable = false)
  LocalDate date;
}
