package org.example.backend.service;

import org.example.backend.exceptions.ResourceNotFoundException;
import org.example.backend.dto.UserCreateRequest;
import org.example.backend.dto.UserResponse;
import org.example.backend.exceptions.APIException;
import org.example.backend.model.User;
import org.example.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long userId) {
        return toResponse(findUserById(userId));
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new APIException("User with email " + request.getEmail() + " already exists");
        });

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(UserCreateRequest request, Long userId) {
        User existingUser = findUserById(userId);
        userRepository.findByEmail(request.getEmail())
                .filter(user -> !user.getId().equals(userId))
                .ifPresent(user -> {
                    throw new APIException("User with email " + request.getEmail() + " already exists");
                });

        existingUser.setName(request.getName());
        existingUser.setEmail(request.getEmail());
        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));

        return toResponse(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}

