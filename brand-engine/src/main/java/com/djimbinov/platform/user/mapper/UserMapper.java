package com.djimbinov.platform.user.mapper;

import com.djimbinov.platform.user.dto.UserResponse;
import com.djimbinov.platform.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponse toResponse(User user) {
    return new UserResponse(
          user.getId(),
          user.getEmail(),
          user.getRole(),
          user.getCreatedAt(),
          user.getUpdatedAt()
    );
  }
}