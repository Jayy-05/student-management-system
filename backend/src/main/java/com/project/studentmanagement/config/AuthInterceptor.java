package com.project.studentmanagement.config;

import com.project.studentmanagement.exception.UnauthorizedException;
import com.project.studentmanagement.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // Allow public auth endpoints
        if (path.startsWith("/api/auth/login")) {
            return true;
        }

        // Allow public read GET requests so visitors can browse the system
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // For POST, PUT, DELETE, require a valid token
        String token = request.getHeader("X-Auth-Token");
        if (token == null || token.isBlank()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
            }
        }

        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Authentication required to perform this action. Please log in.");
        }

        authService.validateToken(token);
        return true;
    }
}
