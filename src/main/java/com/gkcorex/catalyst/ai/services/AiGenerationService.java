package com.gkcorex.catalyst.ai.services;

import reactor.core.publisher.Flux;

public interface AiGenerationService {
  Flux<String> streamResponse(String message, Long projectId);
}
