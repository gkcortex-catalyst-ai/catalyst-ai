package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.entities.*;
import com.gkcorex.catalyst.ai.enums.ChatEventType;
import com.gkcorex.catalyst.ai.enums.MessageRole;
import com.gkcorex.catalyst.ai.exceptions.ResourceNotFoundException;
import com.gkcorex.catalyst.ai.llm.LlmResponseParser;
import com.gkcorex.catalyst.ai.llm.PromptUtils;
import com.gkcorex.catalyst.ai.llm.advisors.FileTreeContextAdvisor;
import com.gkcorex.catalyst.ai.llm.tools.CodeGenerationTools;
import com.gkcorex.catalyst.ai.repositories.*;
import com.gkcorex.catalyst.ai.security.JwtAuthUtil;
import com.gkcorex.catalyst.ai.services.AiGenerationService;
import com.gkcorex.catalyst.ai.services.ProjectFileService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {
  UserRepository userRepository;

  ChatClient chatClient;

  JwtAuthUtil jwtAuthUtil;

  ProjectFileService projectFileService;

  FileTreeContextAdvisor fileTreeContextAdvisor;

  LlmResponseParser llmResponseParser;

  ChatSessionRepository chatSessionRepository;

  ProjectRepository projectRepository;

  ChatMessageRepository chatMessageRepository;

  ChatEventRepository chatEventRepository;

  static Pattern FILE_TAG_PATTERN =
      Pattern.compile("<file path=\"([^\"]+)\">(.*?)</file>", Pattern.DOTALL);

  @Override
  @PreAuthorize("@security.canEditProject(#projectId)")
  public Flux<String> streamResponse(String userMessage, Long projectId) {
    Long userId = jwtAuthUtil.getCurrentUserId();
    ChatSession chatSession = createChatSessionIfNotExists(projectId, userId);
    Map<String, Object> advisorParams =
        Map.of(
            "userId", userId,
            "projectId", projectId);

    StringBuilder fullResponseBuffer = new StringBuilder();

    CodeGenerationTools codeGenerationTools =
        new CodeGenerationTools(projectFileService, projectId);

    AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
    AtomicReference<Long> endTime = new AtomicReference<>(0L);

    return chatClient
        .prompt()
        .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
        .user(userMessage)
        .tools(codeGenerationTools)
        .advisors(
            advisorSpec -> {
              advisorSpec.params(advisorParams);
              advisorSpec.advisors(fileTreeContextAdvisor);
            })
        .stream()
        .chatResponse()
        .doOnNext(
            chatResponse -> {
              String content = chatResponse.getResult().getOutput().getText();
              if (content != null && !content.isEmpty() && endTime.get() == 0) {
                endTime.set(System.currentTimeMillis());
              }
              fullResponseBuffer.append(content);
            })
        .doOnComplete(
            () -> {
              // async updating files inside minio due to heavy lifting or more computation
              Schedulers.boundedElastic()
                  .schedule(
                      () -> {
                        long duration = (endTime.get() - startTime.get()) / 1000;
                        finalizeChats(
                            userMessage, chatSession, fullResponseBuffer.toString(), duration);
                      });
            })
        .doOnError(error -> log.error("Error during streaming for project response"))
        .map(
            chatResponse -> Objects.requireNonNull(chatResponse.getResult().getOutput().getText()));
  }

  private void finalizeChats(
      String userMessage, ChatSession chatSession, String fullText, Long duration) {
    Long projectId = chatSession.getProject().getId();

    // Save the User Message
    chatMessageRepository.save(
        ChatMessage.builder()
            .chatSession(chatSession)
            .role(MessageRole.USER)
            .content(userMessage)
            .build());

    ChatMessage assistantChatMessage =
        ChatMessage.builder()
            .role(MessageRole.ASSISTANT)
            .content("Assistant Message here")
            .chatSession(chatSession)
            .build();

    assistantChatMessage = chatMessageRepository.save(assistantChatMessage);

    List<ChatEvent> chatEvents = llmResponseParser.parseChatEvents(fullText, assistantChatMessage);
    chatEvents.addFirst(
        ChatEvent.builder()
            .type(ChatEventType.THOUGHT)
            .chatMessage(assistantChatMessage)
            .content("Thought for " + duration + "s")
            .sequenceOrder(0)
            .build());

    chatEvents.stream()
        .filter(e -> e.getType() == ChatEventType.FILE_EDIT)
        .forEach(e -> projectFileService.saveFile(projectId, e.getFilePath(), e.getContent()));

    chatEventRepository.saveAll(chatEvents);
  }

  private ChatSession createChatSessionIfNotExists(Long projectId, Long userId) {
    ChatSessionId chatSessionId = new ChatSessionId(projectId, userId);

    ChatSession chatSession = chatSessionRepository.findById(chatSessionId).orElse(null);

    if (chatSession == null) {
      Project project =
          projectRepository
              .findById(projectId)
              .orElseThrow(
                  () -> new ResourceNotFoundException("Project not found", projectId.toString()));

      User user =
          userRepository
              .findById(userId)
              .orElseThrow(
                  () -> new ResourceNotFoundException("User not found", userId.toString()));

      chatSession = ChatSession.builder().id(chatSessionId).project(project).user(user).build();

      chatSession = chatSessionRepository.save(chatSession);
    }
    return chatSession;
  }
}
