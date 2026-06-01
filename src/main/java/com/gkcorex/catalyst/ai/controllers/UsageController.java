package com.gkcorex.catalyst.ai.controllers;

import com.gkcorex.catalyst.ai.services.UsageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UsageController {

  UsageService usageService;

  //  @GetMapping("/today")
  //  public ResponseEntity<UsageTodayResponse> getTodayUsage() {
  //    Long userId = 1L;
  //    return ResponseEntity.ok(usageService.getTodayUsageOfUser(userId));
  //  }

}
