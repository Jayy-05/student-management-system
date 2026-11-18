package com.project.studentmanagement.controller;

import com.project.studentmanagement.dto.LoginRequest;
import com.project.studentmanagement.dto.LoginResponse;
import com.project.studentmanagement.entity.User;
import com.project.studentmanagement.exception.UnauthorizedException;
import com.project.studentmanagement.repository.UserRepository;
import com.project.studentmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String xAuthToken) {
        String token = extractToken(authHeader, xAuthToken);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String xAuthToken) {
        String token = extractToken(authHeader, xAuthToken);
        User user = authService.validateToken(token);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName() != null ? user.getFullName() : "",
                "role", user.getRole()
        ));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String xAuthToken,
            @RequestBody Map<String, String> body) {
        String token = extractToken(authHeader, xAuthToken);
        User user = authService.validateToken(token);

        String oldPass = body.get("oldPassword");
        String newPass = body.get("newPassword");

        if (newPass == null || newPass.trim().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }

        if (!user.getPassword().equals(authService.hashPassword(oldPass))) {
            throw new UnauthorizedException("Incorrect current password");
        }

        user.setPassword(authService.hashPassword(newPass.trim()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    private String extractToken(String authHeader, String xAuthToken) {
        if (xAuthToken != null && !xAuthToken.isBlank()) {
            return xAuthToken.trim();
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return authHeader;
    }
}
