package org.example.backend.controller;

import org.example.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        Map<String, Object> response = userRepository.findByEmail(authentication.getName())
                .<Map<String, Object>>map(user -> Map.of(
                        "id", user.getId(),
                        "username", user.getEmail(),
                        "name", user.getName()
                ))
                .orElseGet(() -> Map.of("username", authentication.getName()));

        return ResponseEntity.ok(response);
    }
}
