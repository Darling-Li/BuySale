package com.rice.trade.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rice.trade.entity.AuditLog;
import com.rice.trade.mapper.AuditLogMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

public class AdminOperationLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AdminOperationLogFilter.class);

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    public AdminOperationLogFilter(AuditLogMapper auditLogMapper, ObjectMapper objectMapper) {
        this.auditLogMapper = auditLogMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!shouldLog(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        filterChain.doFilter(wrappedRequest, response);
        writeAuditLog(wrappedRequest, response);
    }

    private void writeAuditLog(ContentCachingRequestWrapper request, HttpServletResponse response) {
        if (!shouldLog(request)) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return;
        }

        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUsername(authentication.getName());
            auditLog.setRoleName("ADMIN");
            auditLog.setMethod(request.getMethod());
            auditLog.setPath(limit(request.getRequestURI(), 255));
            auditLog.setActionName(limit(request.getMethod() + " " + request.getRequestURI(), 120));
            auditLog.setRequestParams(requestParamsJson(request));
            auditLog.setStatusCode(response.getStatus());
            auditLog.setIpAddress(limit(clientIp(request), 80));
            auditLog.setUserAgent(limit(request.getHeader("User-Agent"), 255));
            auditLog.setOccurredAt(LocalDateTime.now());
            auditLogMapper.insert(auditLog);
        } catch (RuntimeException ex) {
            log.warn("Failed to write admin audit log", ex);
        }
    }

    private boolean shouldLog(HttpServletRequest request) {
        String method = request.getMethod();
        return request.getRequestURI().startsWith("/api/")
                && !"GET".equalsIgnoreCase(method)
                && !"OPTIONS".equalsIgnoreCase(method);
    }

    private String requestParamsJson(ContentCachingRequestWrapper request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Object> parameters = requestParameters(request);
        if (!parameters.isEmpty()) {
            payload.put("parameters", parameters);
        }

        Object body = requestBody(request);
        if (body != null) {
            payload.put("body", body);
        }

        try {
            return objectMapper.writeValueAsString(payload.isEmpty() ? Map.of() : payload);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize request params", ex);
            return "{}";
        }
    }

    private Map<String, Object> requestParameters(HttpServletRequest request) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values == null || values.length == 0) {
                parameters.put(key, "");
            } else if (values.length == 1) {
                parameters.put(key, values[0]);
            } else {
                parameters.put(key, Arrays.asList(values));
            }
        });
        return parameters;
    }

    private Object requestBody(ContentCachingRequestWrapper request) {
        byte[] content = decryptedBody(request);
        if (content == null) {
            content = request.getContentAsByteArray();
        }
        if (content.length == 0) {
            return null;
        }

        String body = new String(content, requestCharset(request));
        if (body.isBlank()) {
            return null;
        }

        if (isJsonContent(request.getContentType())) {
            try {
                return objectMapper.readTree(body);
            } catch (JsonProcessingException ignored) {
                return body;
            }
        }

        return body;
    }

    private byte[] decryptedBody(HttpServletRequest request) {
        Object value = request.getAttribute(ApiCryptoFilter.DECRYPTED_BODY_ATTRIBUTE);
        if (value instanceof byte[] body) {
            return body;
        }
        return null;
    }

    private boolean isJsonContent(String contentType) {
        return contentType != null && contentType.toLowerCase().contains("json");
    }

    private Charset requestCharset(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding == null || encoding.isBlank()) {
            return StandardCharsets.UTF_8;
        }

        try {
            return Charset.forName(encoding);
        } catch (RuntimeException ex) {
            return StandardCharsets.UTF_8;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
