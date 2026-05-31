package com.rice.trade.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rice.trade.config.AdminDeviceProperties;
import com.rice.trade.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class AdminDeviceFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final AdminDeviceProperties properties;
    private final Set<String> allowedTokenHashes;

    public AdminDeviceFilter(ObjectMapper objectMapper, AdminDeviceProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.allowedTokenHashes = cleanList(properties.getAllowedTokenHashes()).stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled() || !isApiPath(request) || isPreflight(request) || !isAdmin()) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(properties.getHeaderName());
        if (token == null || token.isBlank() || !allowedTokenHashes.contains(sha256Hex(token.trim()))) {
            reject(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean isApiPath(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(
                HttpStatus.FORBIDDEN.value(),
                "当前电脑未授权管理员登录",
                null
        ));
    }

    private String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private List<String> cleanList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
    }
}
