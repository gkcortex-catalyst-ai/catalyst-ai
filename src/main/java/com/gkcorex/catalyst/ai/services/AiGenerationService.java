package com.gkcorex.catalyst.ai.services;

import com.gkcorex.catalyst.ai.dtos.chat.StreamResponse;
import reactor.core.publisher.Flux;

public interface AiGenerationService {
  Flux<StreamResponse> streamResponse(String message, Long projectId);
}
