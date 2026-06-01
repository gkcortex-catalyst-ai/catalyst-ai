package com.gkcorex.catalyst.ai.services;

public interface UsageService {

  void recordTokensUsage(Long userId, int actualTokens);

  void checkDailyTokensUsage();
}
