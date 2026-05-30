package com.gkcorex.catalyst.ai.dtos.chat;

import com.gkcorex.catalyst.ai.enums.MessageRole;
import java.time.Instant;
import java.util.List;

public record ChatResponse(
    Long id,
    String content,
    Integer tokensUsed,
    MessageRole role,
    List<ChatEventResponse> events,
    Instant createdAt) {}
