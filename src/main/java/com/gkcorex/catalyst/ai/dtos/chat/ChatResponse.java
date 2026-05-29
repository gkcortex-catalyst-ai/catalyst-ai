package com.gkcorex.catalyst.ai.dtos.chat;

import com.gkcorex.catalyst.ai.entities.ChatEvent;
import com.gkcorex.catalyst.ai.entities.ChatSession;
import com.gkcorex.catalyst.ai.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        Long id,
        ChatSession chatSession,
        String content,
        Integer tokensUsed,
        MessageRole role,
        List<ChatEventResponse>events,
        Instant createdAt
) {
}
