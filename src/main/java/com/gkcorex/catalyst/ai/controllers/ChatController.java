package com.gkcorex.catalyst.ai.controllers;

import com.gkcorex.catalyst.ai.dtos.chat.ChatRequest;
import com.gkcorex.catalyst.ai.dtos.chat.ChatResponse;
import com.gkcorex.catalyst.ai.services.AiGenerationService;
import com.gkcorex.catalyst.ai.services.ChatService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ChatController {

  AiGenerationService aiGenerationService;

  ChatService chatService;

  @PostMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<String>> streamChat(@RequestBody ChatRequest chatRequest) {
    return aiGenerationService
        .streamResponse(chatRequest.message(), chatRequest.projectId())
        .map(data -> ServerSentEvent.<String>builder().data(data).build());
  }

  @GetMapping("/projects/{projectId}")
  public ResponseEntity<List<ChatResponse>> getChatHistory(@PathVariable Long projectId) {
    return ResponseEntity.ok(chatService.getProjectChatHistory(projectId));
  }
}
