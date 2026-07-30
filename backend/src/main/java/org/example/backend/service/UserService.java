package org.example.backend.service;

import org.example.backend.dto.UserCreateRequest;
import org.example.backend.dto.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long userId);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(UserCreateRequest request, Long userId);

    void deleteUser(Long userId);
}

