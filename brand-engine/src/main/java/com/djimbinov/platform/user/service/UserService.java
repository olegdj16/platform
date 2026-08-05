package com.djimbinov.platform.user.service;

import com.djimbinov.platform.auth.exception.EmailAlreadyExistsException;
import com.djimbinov.platform.user.dto.ChangePasswordRequest;
import com.djimbinov.platform.user.dto.UpdateUserRequest;
import com.djimbinov.platform.user.dto.UserResponse;
import com.djimbinov.platform.user.exception.InvalidPasswordException;
import com.djimbinov.platform.user.exception.UserNotFoundException;
import com.djimbinov.platform.user.mapper.UserMapper;
import com.djimbinov.platform.user.model.User;
import com.djimbinov.platform.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public UserService(
        UserRepository userRepository,
        UserMapper userMapper,
        PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional(readOnly = true)
  public UserResponse findCurrentUser(String email) {
    User user = userRepository.findByEmailIgnoreCase(email)
          .orElseThrow(() -> new UserNotFoundException(email));

    return userMapper.toResponse(user);
  }

  @Transactional
  public UserResponse updateCurrentUser(
        String currentEmail,
        UpdateUserRequest request
  ) {
    User user = userRepository.findByEmailIgnoreCase(currentEmail)
          .orElseThrow(() -> new UserNotFoundException(currentEmail));

    String newEmail = request.email()
          .trim()
          .toLowerCase(Locale.ROOT);

    userRepository.findByEmailIgnoreCase(newEmail)
          .filter(existingUser ->
                !existingUser.getId().equals(user.getId())
          )
          .ifPresent(existingUser -> {
            throw new EmailAlreadyExistsException(newEmail);
          });

    user.setEmail(newEmail);
    user.setUpdatedAt(Instant.now());

    User updatedUser = userRepository.save(user);

    return userMapper.toResponse(updatedUser);
  }

  @Transactional
  public void changePassword(
        String email,
        ChangePasswordRequest request
  ) {
    User user = userRepository.findByEmailIgnoreCase(email)
          .orElseThrow(() -> new UserNotFoundException(email));

    boolean currentPasswordMatches = passwordEncoder.matches(
          request.currentPassword(),
          user.getPasswordHash()
    );

    if (!currentPasswordMatches) {
      throw new InvalidPasswordException();
    }

    user.setPasswordHash(
          passwordEncoder.encode(request.newPassword())
    );

    user.setUpdatedAt(Instant.now());

    userRepository.save(user);
  }
}