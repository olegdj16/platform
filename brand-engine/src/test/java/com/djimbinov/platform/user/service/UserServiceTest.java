package com.djimbinov.platform.user.service;

import com.djimbinov.platform.auth.exception.EmailAlreadyExistsException;
import com.djimbinov.platform.user.dto.ChangePasswordRequest;
import com.djimbinov.platform.user.dto.UpdateUserRequest;
import com.djimbinov.platform.user.dto.UserResponse;
import com.djimbinov.platform.user.exception.InvalidPasswordException;
import com.djimbinov.platform.user.exception.UserNotFoundException;
import com.djimbinov.platform.user.mapper.UserMapper;
import com.djimbinov.platform.user.model.User;
import com.djimbinov.platform.user.model.UserRole;
import com.djimbinov.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(
          userRepository,
          userMapper,
          passwordEncoder
    );
  }

  @Test
  void shouldReturnCurrentUserWhenUserExists() {
    String email = "oleg@example.com";
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();

    User user = new User(
          userId,
          email,
          "encoded-password",
          UserRole.USER,
          now,
          now
    );

    UserResponse expectedResponse = new UserResponse(
          userId,
          email,
          UserRole.USER,
          now,
          now
    );

    when(userRepository.findByEmailIgnoreCase(email))
          .thenReturn(Optional.of(user));

    when(userMapper.toResponse(user))
          .thenReturn(expectedResponse);

    UserResponse result = userService.findCurrentUser(email);

    assertSame(expectedResponse, result);
    assertEquals(userId, result.id());
    assertEquals(email, result.email());

    verify(userRepository).findByEmailIgnoreCase(email);
    verify(userMapper).toResponse(user);
  }

  @Test
  void shouldThrowUserNotFoundExceptionWhenCurrentUserDoesNotExist() {
    String email = "missing@example.com";

    when(userRepository.findByEmailIgnoreCase(email))
          .thenReturn(Optional.empty());

    UserNotFoundException exception = assertThrows(
          UserNotFoundException.class,
          () -> userService.findCurrentUser(email)
    );

    assertEquals(
          "User not found: " + email,
          exception.getMessage()
    );

    verify(userRepository).findByEmailIgnoreCase(email);
    verifyNoInteractions(userMapper);
  }

  @Test
  void shouldUpdateCurrentUserEmail() {
    String currentEmail = "oleg16@gmail.com";
    String updatedEmail = "oleg.updated1@example.com";
    UUID userId = UUID.randomUUID();

    Instant originalUpdatedAt =
          Instant.parse("2026-08-05T00:00:00Z");

    User user = new User(
          userId,
          currentEmail,
          "encoded-password",
          UserRole.USER,
          originalUpdatedAt,
          originalUpdatedAt
    );

    UpdateUserRequest request =
          new UpdateUserRequest(
                " OLEG.UPDATED1@EXAMPLE.COM "
          );

    Instant responseTime = Instant.now();

    UserResponse expectedResponse = new UserResponse(
          userId,
          updatedEmail,
          UserRole.USER,
          originalUpdatedAt,
          responseTime
    );

    when(userRepository.findByEmailIgnoreCase(currentEmail))
          .thenReturn(Optional.of(user));

    when(userRepository.findByEmailIgnoreCase(updatedEmail))
          .thenReturn(Optional.empty());

    when(userRepository.save(user))
          .thenReturn(user);

    when(userMapper.toResponse(user))
          .thenReturn(expectedResponse);

    UserResponse result =
          userService.updateCurrentUser(
                currentEmail,
                request
          );

    assertSame(expectedResponse, result);
    assertEquals(updatedEmail, user.getEmail());
    assertNotEquals(
          originalUpdatedAt,
          user.getUpdatedAt()
    );

    verify(userRepository)
          .findByEmailIgnoreCase(currentEmail);

    verify(userRepository)
          .findByEmailIgnoreCase(updatedEmail);

    verify(userRepository).save(user);
    verify(userMapper).toResponse(user);
  }

  @Test
  void shouldThrowEmailAlreadyExistsExceptionWhenUpdatingEmail() {
    String currentEmail = "oleg@example.com";
    String duplicateEmail = "existing@example.com";
    Instant now = Instant.now();

    User currentUser = new User(
          UUID.randomUUID(),
          currentEmail,
          "encoded-password",
          UserRole.USER,
          now,
          now
    );

    User existingUser = new User(
          UUID.randomUUID(),
          duplicateEmail,
          "encoded-password",
          UserRole.USER,
          now,
          now
    );

    UpdateUserRequest request =
          new UpdateUserRequest(duplicateEmail);

    when(userRepository.findByEmailIgnoreCase(currentEmail))
          .thenReturn(Optional.of(currentUser));

    when(userRepository.findByEmailIgnoreCase(duplicateEmail))
          .thenReturn(Optional.of(existingUser));

    EmailAlreadyExistsException exception = assertThrows(
          EmailAlreadyExistsException.class,
          () -> userService.updateCurrentUser(
                currentEmail,
                request
          )
    );

    assertEquals(
          "Email already exists: " + duplicateEmail,
          exception.getMessage()
    );

    verify(userRepository)
          .findByEmailIgnoreCase(currentEmail);

    verify(userRepository)
          .findByEmailIgnoreCase(duplicateEmail);

    verify(userRepository, never())
          .save(any(User.class));

    verifyNoInteractions(userMapper);
  }

  @Test
  void shouldChangePasswordWhenCurrentPasswordIsCorrect() {
    String email = "oleg@example.com";
    String currentPassword = "Password123!";
    String newPassword = "NewPassword123!";
    String oldHash = "old-encoded-password";
    String newHash = "new-encoded-password";

    Instant originalUpdatedAt =
          Instant.parse("2026-08-05T00:00:00Z");

    User user = new User(
          UUID.randomUUID(),
          email,
          oldHash,
          UserRole.USER,
          originalUpdatedAt,
          originalUpdatedAt
    );

    ChangePasswordRequest request =
          new ChangePasswordRequest(
                currentPassword,
                newPassword
          );

    when(userRepository.findByEmailIgnoreCase(email))
          .thenReturn(Optional.of(user));

    when(passwordEncoder.matches(
          currentPassword,
          oldHash
    )).thenReturn(true);

    when(passwordEncoder.encode(newPassword))
          .thenReturn(newHash);

    userService.changePassword(email, request);

    assertEquals(
          newHash,
          user.getPasswordHash()
    );

    assertNotEquals(
          originalUpdatedAt,
          user.getUpdatedAt()
    );

    verify(userRepository)
          .findByEmailIgnoreCase(email);

    verify(passwordEncoder)
          .matches(currentPassword, oldHash);

    verify(passwordEncoder)
          .encode(newPassword);

    verify(userRepository).save(user);
  }

  @Test
  void shouldThrowInvalidPasswordExceptionWhenCurrentPasswordIsIncorrect() {
    String email = "oleg@example.com";
    String oldHash = "old-encoded-password";
    Instant now = Instant.now();

    User user = new User(
          UUID.randomUUID(),
          email,
          oldHash,
          UserRole.USER,
          now,
          now
    );

    ChangePasswordRequest request =
          new ChangePasswordRequest(
                "WrongPassword123!",
                "NewPassword123!"
          );

    when(userRepository.findByEmailIgnoreCase(email))
          .thenReturn(Optional.of(user));

    when(passwordEncoder.matches(
          request.currentPassword(),
          oldHash
    )).thenReturn(false);

    InvalidPasswordException exception = assertThrows(
          InvalidPasswordException.class,
          () -> userService.changePassword(
                email,
                request
          )
    );

    assertEquals(
          "Current password is incorrect",
          exception.getMessage()
    );

    verify(userRepository)
          .findByEmailIgnoreCase(email);

    verify(passwordEncoder).matches(
          request.currentPassword(),
          oldHash
    );

    verify(passwordEncoder, never())
          .encode(any());

    verify(userRepository, never())
          .save(any(User.class));
  }

  @Test
  void shouldThrowUserNotFoundExceptionWhenChangingPasswordForMissingUser() {
    String email = "missing@example.com";

    ChangePasswordRequest request =
          new ChangePasswordRequest(
                "Password123!",
                "NewPassword123!"
          );

    when(userRepository.findByEmailIgnoreCase(email))
          .thenReturn(Optional.empty());

    UserNotFoundException exception = assertThrows(
          UserNotFoundException.class,
          () -> userService.changePassword(
                email,
                request
          )
    );

    assertEquals(
          "User not found: " + email,
          exception.getMessage()
    );

    verify(userRepository)
          .findByEmailIgnoreCase(email);

    verifyNoInteractions(passwordEncoder);

    verify(userRepository, never())
          .save(any(User.class));
  }

  @Test
  void shouldThrowUserNotFoundExceptionWhenUpdatingMissingUser() {
    String email = "missing@example.com";

    UpdateUserRequest request =
          new UpdateUserRequest(
                "updated@example.com"
          );

    when(userRepository.findByEmailIgnoreCase(email))
          .thenReturn(Optional.empty());

    UserNotFoundException exception = assertThrows(
          UserNotFoundException.class,
          () -> userService.updateCurrentUser(
                email,
                request
          )
    );

    assertEquals(
          "User not found: " + email,
          exception.getMessage()
    );

    verify(userRepository)
          .findByEmailIgnoreCase(email);

    verify(userRepository, never())
          .save(any(User.class));

    verifyNoInteractions(userMapper);
  }

  @Test
  void shouldAllowCurrentUserToKeepSameEmail() {
    String email = "oleg@example.com";
    Instant now = Instant.now();

    User user = new User(
          UUID.randomUUID(),
          email,
          "encoded-password",
          UserRole.USER,
          now,
          now
    );

    UpdateUserRequest request =
          new UpdateUserRequest(email);

    UserResponse response = new UserResponse(
          user.getId(),
          email,
          UserRole.USER,
          now,
          now
    );

    when(userRepository.findByEmailIgnoreCase(email))
          .thenReturn(Optional.of(user));

    when(userRepository.save(user))
          .thenReturn(user);

    when(userMapper.toResponse(user))
          .thenReturn(response);

    UserResponse result =
          userService.updateCurrentUser(
                email,
                request
          );

    assertSame(response, result);

    verify(userRepository, times(2))
          .findByEmailIgnoreCase(email);

    verify(userRepository).save(user);
    verify(userMapper).toResponse(user);
  }
}