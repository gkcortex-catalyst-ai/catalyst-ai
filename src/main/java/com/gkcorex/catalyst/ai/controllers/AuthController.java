package com.gkcorex.catalyst.ai.controllers;

import com.gkcorex.catalyst.ai.dtos.auth.AuthResponse;
import com.gkcorex.catalyst.ai.dtos.auth.LoginRequest;
import com.gkcorex.catalyst.ai.dtos.auth.SignUpRequest;
import com.gkcorex.catalyst.ai.dtos.auth.UserProfileResponse;
import com.gkcorex.catalyst.ai.services.AuthService;
import com.gkcorex.catalyst.ai.services.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// @RequestMapping("/api/auth")
@RequestMapping("/api/v1/account/auth")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthController {

  AuthService authService;

  UserService userService;

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signup(@RequestBody @Valid SignUpRequest signUpRequest) {
    return ResponseEntity.ok(authService.signup(signUpRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
    return ResponseEntity.ok(authService.login(loginRequest));
  }

  @GetMapping("/me")
  public ResponseEntity<UserProfileResponse> getProfile() {
    Long userId = 1L;
    return ResponseEntity.ok(userService.getProfile(userId));
  }
}
