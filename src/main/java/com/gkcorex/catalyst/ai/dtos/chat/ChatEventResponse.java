package com.gkcorex.catalyst.ai.dtos.chat;

import com.gkcorex.catalyst.ai.enums.ChatEventType;

public record ChatEventResponse(
    Long id,
    Integer sequenceOrder,
    String content,
    String filePath,
    String metadata,
    ChatEventType type) {}
