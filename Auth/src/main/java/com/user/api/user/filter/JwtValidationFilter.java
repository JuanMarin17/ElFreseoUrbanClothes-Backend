package com.user.api.user.filter;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String userId = request.getHeader("X-User-Id");
        String userName = request.getHeader("X-User-Name");
        String roleId = request.getHeader("X-Role-Id");

        if(userId == null || userName == null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Request not authorized by geteway\"}");
            return;
        }

        request.setAttribute("userName", userName);
        request.setAttribute("user_id", UUID.fromString(userId));
        request.setAttribute("role", roleId);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.contains("/register") ||
                path.contains("/login") ||
                path.contains("/verificationCode") ||
                path.contains("/refresh-token") ||
                path.contains("/resendVerificationCode") || 
                path.contains("/forgotPassword") ||
                path.contains("/forgotPasswordSecondStep") ||
                path.contains("/getEmailById");
    }
}
