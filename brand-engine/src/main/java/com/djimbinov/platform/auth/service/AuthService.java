package com.djimbinov.platform.auth.service;

import com.djimbinov.platform.auth.dto.AuthResponse;
import com.djimbinov.platform.auth.dto.LoginRequest;
import com.djimbinov.platform.auth.dto.RegisterRequest;
import com.djimbinov.platform.auth.exception.EmailAlreadyExistsException;
import com.djimbinov.platform.auth.security.JwtService;
import com.djimbinov.platform.user.model.User;
import com.djimbinov.platform.user.model.UserRole;
import com.djimbinov.platform.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String normalizedEmail = normalizeEmail(request.email());

    if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
      throw new EmailAlreadyExistsException(normalizedEmail);
    }

    Instant now = Instant.now();

    User user = new User(
          UUID.randomUUID(),
          normalizedEmail,
          passwordEncoder.encode(request.password()),
          UserRole.USER,
          now,
          now
    );

    User savedUser = userRepository.save(user);
    UserDetails userDetails = toUserDetails(savedUser);
    String token = jwtService.generateToken(userDetails);

    return new AuthResponse(
          token,
          "Bearer",
          savedUser.getId(),
          savedUser.getEmail()
    );
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    String normalizedEmail = normalizeEmail(request.email());

    authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
                normalizedEmail,
                request.password()
          )
    );

    User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
          .orElseThrow(() ->
                new UsernameNotFoundException(
                      "User not found: " + normalizedEmail
                )
          );

    UserDetails userDetails = toUserDetails(user);
    String token = jwtService.generateToken(userDetails);

    return new AuthResponse(
          token,
          "Bearer",
          user.getId(),
          user.getEmail()
    );
  }

  private UserDetails toUserDetails(User user) {
    return org.springframework.security.core.userdetails.User
          .withUsername(user.getEmail())
          .password(user.getPasswordHash())
          .roles(user.getRole().name())
          .build();
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}