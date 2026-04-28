package com.chat.app.config;

import com.chat.app.service.ClerkJwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ClerkJwtFilter extends OncePerRequestFilter {

    @Autowired
    private ClerkJwtService clerkJwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            String requestURI = request.getRequestURI();

            System.out.println("Processing request: " + requestURI);

            if (token != null && !token.isEmpty()) {
                try {
                    String userId = clerkJwtService.verifyToken(token);
                    System.out.println("Token verified for user: " + userId);
                    request.setAttribute("clerkUserId", userId);
                    request.setAttribute("clerkToken", token);
                } catch (Exception e) {
                    System.err.println("JWT validation error for " + requestURI + ": " + e.getMessage());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: " + e.getMessage());
                    return;
                }
            } else if (!shouldNotFilter(request)) {
                System.err.println("No token provided for protected endpoint: " + requestURI);
            }
        } catch (Exception e) {
            System.err.println("Filter error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip filter for health check and other public endpoints
        return path.equals("/api/health") || path.startsWith("/actuator");
    }
}
