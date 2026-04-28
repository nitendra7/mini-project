package com.chat.app.config;

import com.chat.app.service.ClerkJwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class ClerkWebSocketInterceptor extends HttpSessionHandshakeInterceptor {

    @Autowired
    private ClerkJwtService clerkJwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            // Browsers cannot send custom headers over WebSocket/SockJS connections,
            // so the token is passed as a query parameter: /ws?token=<jwt>
            String token = extractToken(httpRequest);

            if (token != null && !token.isEmpty()) {
                try {
                    String userId = clerkJwtService.verifyToken(token);
                    attributes.put("userId", userId);
                    attributes.put("clerkToken", token);
                    return super.beforeHandshake(request, response, wsHandler, attributes);
                } catch (Exception e) {
                    System.err.println("Token validation failed: " + e.getMessage());
                    return false;
                }
            } else {
                System.err.println("No token found in WebSocket handshake request");
                return false;
            }
        }
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Try query parameter first (required for browser WebSocket / SockJS)
        String queryToken = request.getParameter("token");
        if (queryToken != null && !queryToken.isEmpty()) {
            return queryToken;
        }
        // 2. Fall back to Authorization header (for non-browser clients)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
