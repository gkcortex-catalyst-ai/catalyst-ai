package com.gkcorex.catalyst.ai.services.impl;

import com.gkcorex.catalyst.ai.llm.PromptUtils;
import com.gkcorex.catalyst.ai.security.JwtAuthUtil;
import com.gkcorex.catalyst.ai.services.AiGenerationService;
import com.gkcorex.catalyst.ai.services.ProjectFileService;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
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

  ChatClient chatClient;

  JwtAuthUtil jwtAuthUtil;

  ProjectFileService projectFileService;

  static Pattern FILE_TAG_PATTERN =
      Pattern.compile("<file path=\"([^\"]+)\">(.*?)</file>", Pattern.DOTALL);

  @Override
  @PreAuthorize("@security.canEditProject(#projectId)")
  public Flux<String> streamResponse(String userMessage, Long projectId) {
    Long userId = jwtAuthUtil.getCurrentUserId();
    // create project

    // create new chat session
    createChatSessionIfNotExists(projectId, userId);
    Map<String, Object> advisorParams =
        Map.of(
            "userId", userId,
            "projectId", projectId);

    StringBuilder fullResponseBuffer = new StringBuilder();

    return chatClient
        .prompt()
        .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
        .user(userMessage)
        .advisors(
            advisorSpec -> {
              advisorSpec.params(advisorParams);
            })
        .stream()
        .chatResponse()
        .doOnNext(
            chatResponse -> {
              String content = chatResponse.getResult().getOutput().getText();
              fullResponseBuffer.append(content);
            })
        .doOnComplete(
            () -> {
              // async updating files inside minio due to heavy lifting or more computation
              Schedulers.boundedElastic()
                  .schedule(
                      () -> {
                        parseAndSaveFiles(fullResponseBuffer.toString(), projectId);
                      });
            })
        .doOnError(error -> log.error("Error during streaming for project response"))
        .map(
            chatResponse -> Objects.requireNonNull(chatResponse.getResult().getOutput().getText()));
  }

  private void parseAndSaveFiles(String fullResponse, Long projectId) {
    Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);
    while (matcher.find()) {
      String filePath = matcher.group(1);
      String fileContent = matcher.group(2).trim();

      projectFileService.saveFile(projectId, filePath, fileContent);
    }
  }

  private void createChatSessionIfNotExists(Long projectId, Long userId) {}
}
