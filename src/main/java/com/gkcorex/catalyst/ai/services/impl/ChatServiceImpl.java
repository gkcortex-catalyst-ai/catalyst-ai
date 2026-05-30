package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.dtos.chat.ChatResponse;
import com.gkcorex.catalyst.ai.entities.ChatMessage;
import com.gkcorex.catalyst.ai.entities.ChatSession;
import com.gkcorex.catalyst.ai.entities.ChatSessionId;
import com.gkcorex.catalyst.ai.mappers.ChatMapper;
import com.gkcorex.catalyst.ai.repositories.ChatMessageRepository;
import com.gkcorex.catalyst.ai.repositories.ChatSessionRepository;
import com.gkcorex.catalyst.ai.security.JwtAuthUtil;
import com.gkcorex.catalyst.ai.services.ChatService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ChatServiceImpl implements ChatService {

  ChatMessageRepository chatMessageRepository;

  ChatSessionRepository chatSessionRepository;

  JwtAuthUtil jwtAuthUtil;

  ChatMapper chatMapper;

  @Override
  public List<ChatResponse> getProjectChatHistory(Long projectId) {
    Long userId = jwtAuthUtil.getCurrentUserId();

    ChatSession chatSession =
        chatSessionRepository.getReferenceById(new ChatSessionId(projectId, userId));

    List<ChatMessage> chatMessages = chatMessageRepository.findByChatSession(chatSession);

    return chatMapper.mapChatMessagesToChatResponses(chatMessages);
  }
}
