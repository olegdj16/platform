package com.djimbinov.platform.user.controller;

import com.djimbinov.platform.auth.security.JwtAuthenticationFilter;
import com.djimbinov.platform.user.dto.ChangePasswordRequest;
import com.djimbinov.platform.user.dto.UpdateUserRequest;
import com.djimbinov.platform.user.dto.UserResponse;
import com.djimbinov.platform.user.model.UserRole;
import com.djimbinov.platform.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private Authentication authenticationFor(String email) {
    return new UsernamePasswordAuthenticationToken(
          email,
          null,
          List.of()
    );
  }

  @Test
  void shouldReturnCurrentUser() throws Exception {
    String email = "oleg@example.com";
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();

    UserResponse response = new UserResponse(
          userId,
          email,
          UserRole.USER,
          now,
          now
    );

    when(userService.findCurrentUser(email))
          .thenReturn(response);

    mockMvc.perform(
                get("/api/v1/users/me")
                      .principal(authenticationFor(email))
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id")
                .value(userId.toString()))
          .andExpect(jsonPath("$.email")
                .value(email))
          .andExpect(jsonPath("$.role")
                .value("USER"));

    verify(userService).findCurrentUser(email);
  }

  @Test
  void shouldUpdateCurrentUser() throws Exception {
    String currentEmail = "oleg@example.com";
    String updatedEmail = "oleg.updated@example.com";
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();

    UpdateUserRequest request =
          new UpdateUserRequest(updatedEmail);

    UserResponse response = new UserResponse(
          userId,
          updatedEmail,
          UserRole.USER,
          now,
          now
    );

    when(userService.updateCurrentUser(
          currentEmail,
          request
    )).thenReturn(response);

    mockMvc.perform(
                put("/api/v1/users/me")
                      .principal(authenticationFor(currentEmail))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id")
                .value(userId.toString()))
          .andExpect(jsonPath("$.email")
                .value(updatedEmail))
          .andExpect(jsonPath("$.role")
                .value("USER"));

    verify(userService).updateCurrentUser(
          currentEmail,
          request
    );
  }

  @Test
  void shouldChangeCurrentUserPassword() throws Exception {
    String email = "oleg@example.com";

    ChangePasswordRequest request =
          new ChangePasswordRequest(
                "Password123!",
                "NewPassword123!"
          );

    mockMvc.perform(
                put("/api/v1/users/me/password")
                      .principal(authenticationFor(email))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isNoContent());

    verify(userService).changePassword(
          email,
          request
    );
  }

  @Test
  void shouldReturnBadRequestWhenUpdatingEmailIsInvalid()
        throws Exception {

    String email = "oleg@example.com";

    UpdateUserRequest request =
          new UpdateUserRequest("invalid-email");

    mockMvc.perform(
                put("/api/v1/users/me")
                      .principal(authenticationFor(email))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isBadRequest());

    verifyNoInteractions(userService);
  }

  @Test
  void shouldReturnBadRequestWhenNewPasswordIsTooShort()
        throws Exception {

    String email = "oleg@example.com";

    ChangePasswordRequest request =
          new ChangePasswordRequest(
                "Password123!",
                "short"
          );

    mockMvc.perform(
                put("/api/v1/users/me/password")
                      .principal(authenticationFor(email))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isBadRequest());

    verifyNoInteractions(userService);
  }
}