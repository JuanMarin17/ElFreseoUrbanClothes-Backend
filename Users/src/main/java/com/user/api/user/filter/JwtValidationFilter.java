package com.user.api.user.filter;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.user.api.user.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Header Authorization is missing in the request\"}");
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(token)) {
                String username = jwtService.extractUsername(token);
                UUID userId = jwtService.extractUserId(token);
                UUID rolId = jwtService.extractRolId(token);

                request.setAttribute("username", username);
                request.setAttribute("user_id", userId);
                request.setAttribute("rol_id", rolId);

                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid token\"}");
                return;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token processing failed\"}");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.contains("/register") ||
                path.contains("/login") ||
                path.contains("/verificationCode") ||
                path.contains("/refreshToken") ||
                path.contains("/resendVerificationCode");
    }
}
