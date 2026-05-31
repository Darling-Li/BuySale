package com.rice.trade.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rice.trade.config.TokenSessionProperties;
import com.rice.trade.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class TokenSessionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final TokenSessionProperties properties;
    private final TokenSessionService tokenSessionService;

    public TokenSessionFilter(
            ObjectMapper objectMapper,
            TokenSessionProperties properties,
            TokenSessionService tokenSessionService
    ) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.tokenSessionService = tokenSessionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled() || !isApiPath(request) || isPreflight(request) || isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        if (!tokenSessionService.tokenExists(authorization)) {
            reject(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "/api/auth/me".equals(request.getRequestURI())
                && tokenSessionService.isLoginRequest(request.getHeader(properties.getLoginHeader()));
    }

    private boolean isApiPath(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(
                HttpStatus.UNAUTHORIZED.value(),
                "登录已过期，请重新登录",
                null
        ));
    }
}
