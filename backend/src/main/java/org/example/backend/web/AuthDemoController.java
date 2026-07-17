package org.example.backend.web;

import java.security.Principal;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthDemoController {

    @GetMapping("/public/health")
    Map<String, String> publicHealth() {
        return Map.of("status", "ok");
    }

    @GetMapping("/user/me")
    Map<String, String> currentUser(Principal principal) {
        return Map.of("username", principal.getName());
    }

    @GetMapping("/admin/status")
    @PreAuthorize("hasRole('ADMIN')")
    Map<String, String> adminStatus() {
        return Map.of("status", "admin access granted");
    }
}
