package com.gkcorex.catalyst.ai.entities;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatSessionId implements Serializable {
  Long projectId;
  Long userId;
}
