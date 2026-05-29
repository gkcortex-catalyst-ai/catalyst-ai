package com.gkcorex.catalyst.ai.services;

import com.gkcorex.catalyst.ai.dtos.chat.ChatResponse;
import com.gkcorex.catalyst.ai.entities.ChatMessage;

import java.util.List;

public interface ChatService {
    List<ChatResponse> getProjectChatHistory(Long projectId);
}
