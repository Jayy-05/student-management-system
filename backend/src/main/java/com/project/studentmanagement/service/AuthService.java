package com.project.studentmanagement.service;

import com.project.studentmanagement.dto.LoginRequest;
import com.project.studentmanagement.dto.LoginResponse;
import com.project.studentmanagement.entity.User;
import com.project.studentmanagement.exception.UnauthorizedException;
import com.project.studentmanagement.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;

    // Token -> (Username, Expiry)
    private static class Session {
        final String username;
        final Instant expiresAt;

        Session(String username, Instant expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, Session> activeTokens = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initDefaultAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User(
                    "admin",
                    hashPassword("admin123"),
                    "System Administrator",
                    "ROLE_ADMIN"
            );
            userRepository.save(admin);
        }
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        String hashed = hashPassword(request.getPassword().trim());
        if (!user.getPassword().equals(hashed)) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");

        // 24 hour expiry
        Instant expiresAt = Instant.now().plusSeconds(24 * 3600);
        activeTokens.put(token, new Session(user.getUsername(), expiresAt));

        return new LoginResponse(token, user.getUsername(), user.getFullName(), user.getRole());
    }

    public void logout(String token) {
        if (token != null) {
            activeTokens.remove(token);
        }
    }

    public User validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Authentication token is required");
        }
        Session session = activeTokens.get(token);
        if (session == null || Instant.now().isAfter(session.expiresAt)) {
            if (session != null) activeTokens.remove(token);
            throw new UnauthorizedException("Session expired or invalid token. Please log in again.");
        }
        return userRepository.findByUsername(session.username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    public boolean isValidToken(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Standard salt to prevent rainbow table attacks
            String salted = "SMS_SALT_2026_" + rawPassword;
            byte[] hash = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
