package com.djimbinov.platform.user.controller;

import com.djimbinov.platform.user.dto.ChangePasswordRequest;
import com.djimbinov.platform.user.dto.UpdateUserRequest;
import com.djimbinov.platform.user.dto.UserResponse;
import com.djimbinov.platform.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser(
        Authentication authentication
  ) {
    UserResponse response =
          userService.findCurrentUser(authentication.getName());

    return ResponseEntity.ok(response);
  }

  @PutMapping("/me")
  public ResponseEntity<UserResponse> updateCurrentUser(
        Authentication authentication,
        @Valid @RequestBody UpdateUserRequest request
  ) {
    UserResponse response = userService.updateCurrentUser(
          authentication.getName(),
          request
    );

    return ResponseEntity.ok(response);
  }

  @PutMapping("/me/password")
  public ResponseEntity<Void> changePassword(
        Authentication authentication,
        @Valid @RequestBody ChangePasswordRequest request
  ) {
    userService.changePassword(
          authentication.getName(),
          request
    );

    return ResponseEntity.noContent().build();
  }
}