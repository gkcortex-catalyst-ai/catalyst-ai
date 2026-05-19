package com.gkcorex.catalyst.ai.entities;

import com.gkcorex.catalyst.ai.enums.MessageRole;
import jakarta.persistence.*;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "chat_message")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumns({
          @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false),
          @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
  })
  ChatSession chatSession;

  @Column(columnDefinition = "text", nullable = false)
  String content;

//  /*
//     JSON Array of Tools Called
//  */
//  String toolCalls;

  Integer tokensUsed = 0;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false) // USER,ASSISTANT
  MessageRole role;

  @CreationTimestamp
  Instant createdAt;
}
